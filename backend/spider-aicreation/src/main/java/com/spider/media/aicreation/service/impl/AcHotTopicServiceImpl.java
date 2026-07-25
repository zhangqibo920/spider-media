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
import org.springframework.web.reactive.function.client.WebClient;

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
    /** HTTP 客户端（用于抓取各平台热搜 API） */
    private final WebClient webClient = WebClient.builder()
            .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    public AcHotTopicServiceImpl(AcHotTopicMapper hotTopicMapper) {
        this.hotTopicMapper = hotTopicMapper;
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
                default:
                    topics = fetchWeiboHot();
                    break;
            }

            // 先删除该用户在该平台的旧热点数据
            hotTopicMapper.deleteByUserId(userId, platform);

            // 保存新抓取的热点数据
            LocalDateTime now = LocalDateTime.now();
            int saved = 0;
            for (AcHotTopic topic : topics) {
                topic.setUserId(userId);
                topic.setPlatform(platform);
                topic.setCreateBy("system");
                topic.setCreateTime(now);
                try {
                    hotTopicMapper.insert(topic);
                    saved++;
                } catch (Exception e) {
                    log.debug("保存热点话题失败: {}", topic.getTitle());
                }
            }
            log.info("热点抓取完成, platform={}, 新增{}条", platform, saved);
        } catch (Exception e) {
            log.error("热点抓取失败, platform={}", platform, e);
        }
    }

    @Override
    public List<AcHotTopic> selectHotTopicList(Long userId) {
        return hotTopicMapper.selectByUserId(userId);
    }

    /**
     * 抓取微博热搜榜（最多30条）
     */
    private List<AcHotTopic> fetchWeiboHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://weibo.com/ajax/side/hotSearch")
                    .retrieve().bodyToMono(String.class).block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data").path("realtime");
                if (data.isArray()) {
                    int rank = 1;
                    for (JsonNode item : data) {
                        AcHotTopic topic = new AcHotTopic();
                        topic.setTitle(item.path("note").asText(""));
                        topic.setDescription(item.path("label_name").asText(""));
                        topic.setHotScore(item.path("num").asInt(0));
                        topic.setUrl("https://s.weibo.com/weibo?q=%23" + item.path("word").asText("") + "%23");
                        topic.setCategory("微博热搜");
                        topics.add(topic);
                        if (++rank > 30) break;
                    }
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
                    .retrieve().bodyToMono(String.class).block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
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
            String json = webClient.get()
                    .uri("https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=30")
                    .retrieve().bodyToMono(String.class).block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    for (JsonNode item : data) {
                        JsonNode target = item.path("target");
                        AcHotTopic topic = new AcHotTopic();
                        topic.setTitle(target.path("title").asText(""));
                        topic.setDescription(target.path("excerpt").asText(""));
                        topic.setHotScore(item.path("detail_text").asText("0").replaceAll("[^0-9]", "").isEmpty() ? 0 :
                                Integer.parseInt(item.path("detail_text").asText("0").replaceAll("[^0-9]", "")));
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

    /**
     * 抓取今日头条热榜（最多30条）
     */
    private List<AcHotTopic> fetchToutiaoHot() {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc")
                    .retrieve().bodyToMono(String.class).block();
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
