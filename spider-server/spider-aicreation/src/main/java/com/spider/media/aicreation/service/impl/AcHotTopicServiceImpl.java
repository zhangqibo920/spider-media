package com.spider.media.aicreation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.aicreation.mapper.AcHotTopicMapper;
import com.spider.media.aicreation.service.IAcHotTopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 热点话题业务层实现类
 *
 * <p>从各主流平台（微博、抖音、知乎、头条）异步抓取热搜榜单数据，
 * 解析 JSON 响应并保存到数据库。支持按平台分别抓取，每次抓取前会清理旧数据。</p>
 *
 * <p>使用 @Async 注解实现异步执行，避免阻塞前端请求。
 * 使用 WebClient（响应式 HTTP 客户端）发起网络请求。</p>
 */
@Service
public class AcHotTopicServiceImpl implements IAcHotTopicService {

    private static final Logger log = LoggerFactory.getLogger(AcHotTopicServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 热点话题数据访问对象 */
    private final AcHotTopicMapper hotTopicMapper;
    /** HTTP 客户端（用于抓取各平台热搜 API，由 WebClientConfig 统一配置超时） */
    private final WebClient webClient;
    /** 编程式事务模板，用于 @Async 方法内部的事务控制 */
    private final TransactionTemplate transactionTemplate;

    public AcHotTopicServiceImpl(AcHotTopicMapper hotTopicMapper, WebClient.Builder webClientBuilder,
                                  TransactionTemplate transactionTemplate) {
        this.hotTopicMapper = hotTopicMapper;
        this.transactionTemplate = transactionTemplate;
        // 在统一超时配置基础上，添加爬虫专用 Header 和内存缓冲区
        this.webClient = webClientBuilder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "application/json, text/plain, */*")
                .defaultHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .defaultHeader("Referer", "https://www.douyin.com/")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    /**
     * 异步抓取指定平台的热点话题
     *
     * <p>根据平台类型调用对应的抓取方法，抓取后先删除旧数据再保存新数据。
     * 每个平台最多保存 30 条热点话题。</p>
     *
     * @param platform 平台类型（weibo/douyin/zhihu/toutiao）
     * @param userId   用户ID
     */
    @Override
    @Async
    public void fetchHotTopics(String platform, Long userId) {
        log.info("开始抓取热点话题, platform={}, userId={}", platform, userId);
        try {
            List<AcHotTopic> topics = new ArrayList<>();
            switch (platform.toLowerCase()) {
                case "weibo":
                    topics = fetchWeiboHot();
                    break;
                case "douyin":
                    topics = fetchDouyinHot();
                    break;
                case "zhihu":
                    topics = fetchZhihuHot();
                    break;
                case "toutiao":
                    topics = fetchToutiaoHot();
                    break;
                case "baidu":
                    topics = fetchBaiduHot();
                    break;
                case "bilibili":
                    topics = fetchBilibiliHot();
                    break;
                case "hackernews":
                    topics = fetchHackerNewsHot();
                    break;
                case "github":
                    topics = fetchGitHubHot();
                    break;
                default:
                    topics = fetchWeiboHot();
                    break;
            }

            // 在事务中执行数据库操作（@Async 方法中的 @Transactional 会失效）
            List<AcHotTopic> finalTopics = topics;
            int saved = transactionTemplate.execute(status -> {
                hotTopicMapper.deleteByUserId(userId, platform);
                LocalDateTime now = LocalDateTime.now();
                int count = 0;
                for (AcHotTopic topic : finalTopics) {
                    topic.setUserId(userId);
                    topic.setPlatform(platform);
                    topic.setCreateBy("system");
                    topic.setCreateTime(now);
                    try {
                        hotTopicMapper.insert(topic);
                        count++;
                    } catch (Exception e) {
                        log.debug("保存热点话题失败: {}", topic.getTitle());
                    }
                }
                return count;
            });
            log.info("热点抓取完成, platform={}, 新增{}条", platform, saved);
        } catch (Exception e) {
            log.error("热点抓取失败, platform={}", platform, e);
        }
    }

    @Override
    public List<AcHotTopic> selectHotTopicList(Long userId) {
        return hotTopicMapper.selectByUserId(userId);
    }

    @Override
    public void deleteHotTopic(Long id) {
        hotTopicMapper.deleteById(id);
    }

    /**
     * 抓取微博热搜榜（最多30条）
     */
    private List<AcHotTopic> fetchWeiboHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String html = webClient.get()
                    .uri("https://s.weibo.com/top/summary")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (html != null) {
                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
                int rank = 1;
                for (org.jsoup.nodes.Element tr : doc.select("#pl_top_realtimehot table tbody tr")) {
                    String title = tr.select(".td-02 a").text();
                    String hot = tr.select(".td-02 span").text();
                    if (title.isEmpty()) continue;
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(title);
                    topic.setDescription("");
                    topic.setHotScore(parseIntSafe(hot));
                    topic.setUrl("https://s.weibo.com" + tr.select(".td-02 a").attr("href"));
                    topic.setCategory("微博热搜");
                    topics.add(topic);
                    if (++rank > 30) break;
                }
            }
        } catch (Exception e) {
            log.warn("微博热搜抓取失败", e);
        }
        return topics;
    }

    /**
     * 抓取抖音热搜榜（最多30条）
     */
    private List<AcHotTopic> fetchDouyinHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://www.douyin.com/aweme/v1/web/hot/search/list/")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            log.debug("抖音热搜API返回: {}", json != null ? json.substring(0, Math.min(json.length(), 500)) : "null");
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                int statusCode = root.path("status_code").asInt(-1);
                if (statusCode != 0) {
                    log.warn("抖音热搜API返回错误状态码: {}", statusCode);
                    return topics;
                }
                JsonNode data = root.path("data").path("word_list");
                if (data.isArray()) {
                    int rank = 1;
                    for (JsonNode item : data) {
                        AcHotTopic topic = new AcHotTopic();
                        topic.setTitle(item.path("word").asText(""));
                        topic.setDescription(item.path("word_sub_board").asText(""));
                        topic.setHotScore(item.path("hot_value").asInt(0));
                        topic.setUrl("https://www.douyin.com/search/" + item.path("word").asText(""));
                        topic.setCategory("抖音热搜");
                        topics.add(topic);
                        if (++rank > 30) break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("抖音热搜抓取失败", e);
        }
        return topics;
    }

    /**
     * 抓取知乎热榜（最多30条）
     */
    private List<AcHotTopic> fetchZhihuHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String cookie = grabZhihuCookie();
            String json = webClient.get()
                    .uri("https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=30")
                    .header("Cookie", cookie)
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    for (JsonNode item : data) {
                        JsonNode target = item.path("target");
                        AcHotTopic topic = new AcHotTopic();
                        topic.setTitle(target.path("title").asText(""));
                        topic.setDescription(target.path("excerpt").asText(""));
                        String hotText = item.path("detail_text").asText("0").replaceAll("[^0-9]", "");
                        topic.setHotScore(hotText.isEmpty() ? 0 : Integer.parseInt(hotText));
                        topic.setUrl("https://www.zhihu.com/question/" + target.path("id").asText(""));
                        topic.setCategory("知乎热榜");
                        topics.add(topic);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("知乎热榜抓取失败", e);
        }
        return topics;
    }

    private String grabZhihuCookie() {
        try {
            return webClient.get()
                    .uri("https://www.zhihu.com/")
                    .exchangeToMono(resp -> {
                        var cookieMap = resp.cookies();
                        StringBuilder sb = new StringBuilder();
                        cookieMap.forEach((name, values) -> {
                            if (!values.isEmpty()) {
                                if (!sb.isEmpty()) sb.append("; ");
                                sb.append(name).append("=").append(values.get(0).getValue());
                            }
                        });
                        return reactor.core.publisher.Mono.just(sb.toString());
                    })
                    .blockOptional().orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 抓取今日头条热榜（最多30条）
     */
    private List<AcHotTopic> fetchBaiduHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String html = webClient.get()
                    .uri("https://top.baidu.com/board?tab=realtime")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (html != null) {
                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
                org.jsoup.select.Elements items = doc.select(".category-wrap_iQLoo .content-wrapper .content_28Njm");
                int rank = 1;
                for (org.jsoup.nodes.Element item : items) {
                    String title = item.select(".c-single-text-ellipsis").text();
                    String desc = item.select(".desc_3CTjT").text();
                    if (title.isEmpty()) continue;
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(title);
                    topic.setDescription(desc);
                    topic.setHotScore(parseIntSafe(item.select(".hot-index_1Bl1a").text()));
                    topic.setUrl("https://top.baidu.com/board?tab=realtime");
                    topic.setCategory("百度热搜");
                    topics.add(topic);
                    if (++rank > 30) break;
                }
            }
        } catch (Exception e) {
            log.warn("百度热搜抓取失败", e);
        }
        return topics;
    }

    private List<AcHotTopic> fetchBilibiliHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://api.bilibili.com/x/web-interface/ranking/v2")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data").path("list");
                if (data.isArray()) {
                    for (JsonNode item : data) {
                        AcHotTopic topic = new AcHotTopic();
                        topic.setTitle(item.path("title").asText(""));
                        topic.setDescription(item.path("desc").asText(""));
                        topic.setHotScore(item.path("stat").path("view").asInt(0));
                        topic.setUrl("https://www.bilibili.com/video/" + item.path("bvid").asText());
                        topic.setCategory("B站热门");
                        topics.add(topic);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("B站热门抓取失败", e);
        }
        return topics;
    }

    private List<AcHotTopic> fetchHackerNewsHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String idsJson = webClient.get()
                    .uri("https://hacker-news.firebaseio.com/v0/topstories.json")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (idsJson == null) return topics;
            int[] ids = objectMapper.readValue(idsJson, int[].class);
            int count = 0;
            for (int id : ids) {
                if (count >= 30) break;
                String itemJson = webClient.get()
                        .uri("https://hacker-news.firebaseio.com/v0/item/" + id + ".json")
                        .retrieve().bodyToMono(String.class)
                        .retryWhen(Retry.backoff(1, Duration.ofSeconds(1)))
                        .block();
                if (itemJson == null) continue;
                JsonNode item = objectMapper.readTree(itemJson);
                String title = item.path("title").asText("");
                if (title.isEmpty()) continue;
                AcHotTopic topic = new AcHotTopic();
                topic.setTitle(title);
                topic.setDescription(item.path("text").asText(""));
                topic.setHotScore(item.path("score").asInt(0));
                topic.setUrl(item.path("url").asText("https://news.ycombinator.com/item?id=" + id));
                topic.setCategory("HackerNews");
                topics.add(topic);
                count++;
            }
        } catch (Exception e) {
            log.warn("HackerNews抓取失败", e);
        }
        return topics;
    }

    private List<AcHotTopic> fetchGitHubHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String html = webClient.get()
                    .uri("https://github.com/trending")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (html != null) {
                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
                org.jsoup.select.Elements articles = doc.select("article.Box-row");
                for (org.jsoup.nodes.Element article : articles) {
                    String repo = article.select("h2 a").text().replaceAll("\\s+", "");
                    String desc = article.select("p").text();
                    if (repo.isEmpty()) continue;
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(repo);
                    topic.setDescription(desc);
                    topic.setHotScore(parseIntSafe(article.select(".d-inline-block.float-sm-right").text()));
                    topic.setUrl("https://github.com/" + repo);
                    topic.setCategory("GitHub Trending");
                    topics.add(topic);
                }
            }
        } catch (Exception e) {
            log.warn("GitHub Trending抓取失败", e);
        }
        return topics;
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9kK]", "").replaceAll("(?i)k", "000"));
        } catch (Exception e) {
            return 0;
        }
    }

    private List<AcHotTopic> fetchToutiaoHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    int rank = 1;
                    for (JsonNode item : data) {
                        AcHotTopic topic = new AcHotTopic();
                        topic.setTitle(item.path("Title").asText(""));
                        topic.setDescription(item.path("Abstract").asText(""));
                        topic.setHotScore(item.path("HotValue").asInt(0));
                        topic.setUrl(item.path("Url").asText(""));
                        topic.setCategory("头条热榜");
                        topics.add(topic);
                        if (++rank > 30) break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("头条热榜抓取失败", e);
        }
        return topics;
    }
}
