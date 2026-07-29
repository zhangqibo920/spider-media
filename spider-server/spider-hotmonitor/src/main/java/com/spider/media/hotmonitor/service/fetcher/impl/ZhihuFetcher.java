package com.spider.media.hotmonitor.service.fetcher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.service.fetcher.IHotSourceFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ZhihuFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(ZhihuFetcher.class);
    private static final Pattern HOT_SCORE_PATTERN = Pattern.compile("(\\d[\u00a0\\d]*)");

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String configuredCookie;

    public ZhihuFetcher(WebClient.Builder builder,
                        @Value("${fetcher.zhihu.cookie:}") String configuredCookie) {
        this.webClient = builder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "application/json, text/plain, */*")
                .defaultHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .defaultHeader("Referer", "https://www.zhihu.com/hot")
                .defaultHeader("x-requested-with", "XMLHttpRequest")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
        this.configuredCookie = configuredCookie;
    }

    @Override
    public String sourceName() {
        return "zhihu";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = fetchByJsoup(keyword);
        if (!topics.isEmpty()) {
            return topics;
        }
        log.info("Jsoup 解析知乎热榜失败，尝试 API 方式");
        return fetchByApi(keyword);
    }

    private List<AcHotTopic> fetchByJsoup(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://www.zhihu.com/hot")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .timeout(15000)
                    .get();

            List<Element> cards = doc.select(".HotList-item, .Card[data-za-module=HotList]");
            if (cards.isEmpty()) {
                cards = doc.select("[class*=\"HotList-item\"], [class*=\"hot-item\"]");
            }
            if (cards.isEmpty()) {
                cards = doc.select("section[class*=\"HotItem\"]");
            }

            int rank = 0;
            for (Element card : cards) {
                String title = card.select("[class*=\"title\"], [class*=\"Title\"], a").attr("title");
                if (title.isEmpty()) {
                    title = card.select("[class*=\"title\"], [class*=\"Title\"], a").text();
                }
                if (title.isEmpty()) continue;

                if (keyword != null && !keyword.isEmpty() && !title.contains(keyword)) continue;

                String desc = card.select("[class*=\"excerpt\"], [class*=\"summary\"], [class*=\"desc\"]").text();
                String hotText = card.select("[class*=\"hot\"], [class*=\"score\"], [class*=\"metrics\"]").text();
                if (hotText.isEmpty()) {
                    hotText = card.select("[class*=\"HotItem-rank\"]").text();
                }

                String link = card.select("a[href*=\"question\"]").attr("href");
                if (link.isEmpty()) {
                    link = card.select("a").attr("href");
                }
                if (!link.startsWith("http")) {
                    link = "https://www.zhihu.com" + link;
                }

                AcHotTopic topic = new AcHotTopic();
                topic.setTitle(title);
                topic.setDescription(desc);
                topic.setHotScore(parseHotScore(hotText));
                topic.setUrl(link.isEmpty() ? "https://www.zhihu.com/hot" : link);
                topic.setCategory("知乎热榜");
                topics.add(topic);

                if (++rank >= 30) break;
            }

            if (!topics.isEmpty()) {
                log.info("Jsoup 解析知乎热榜成功，获取 {} 条", topics.size());
            }
        } catch (Exception e) {
            log.warn("Jsoup 解析知乎热榜失败: {}", e.getMessage());
        }
        return topics;
    }

    private List<AcHotTopic> fetchByApi(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String cookie = resolveCookie();
            if (cookie.isEmpty()) {
                log.warn("未配置知乎 Cookie，API 方式不可用");
                return topics;
            }

            String json = webClient.get()
                    .uri("https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=30")
                    .header("Cookie", cookie)
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(1)))
                    .block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    for (JsonNode item : data) {
                        JsonNode target = item.path("target");
                        String title = target.path("title").asText("");
                        if (keyword == null || keyword.isEmpty() || title.contains(keyword)) {
                            AcHotTopic topic = new AcHotTopic();
                            topic.setTitle(title);
                            topic.setDescription(target.path("excerpt").asText(""));
                            String hotText = item.path("detail_text").asText("0").replaceAll("[^0-9]", "");
                            topic.setHotScore(hotText.isEmpty() ? 0 : Integer.parseInt(hotText));
                            topic.setUrl("https://www.zhihu.com/question/" + target.path("id").asText(""));
                            topic.setCategory("知乎热榜");
                            topics.add(topic);
                        }
                    }
                }
            }
        } catch (WebClientResponseException e) {
            log.warn("知乎 API 抓取失败 ({}), 请检查 fetcher.zhihu.cookie 是否有效", e.getStatusCode());
        } catch (Exception e) {
            log.warn("知乎 API 抓取异常", e);
        }
        return topics;
    }

    private String resolveCookie() {
        if (configuredCookie != null && !configuredCookie.trim().isEmpty()) {
            return configuredCookie.trim();
        }
        try {
            return webClient.get()
                    .uri("https://www.zhihu.com/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .exchangeToMono(resp -> {
                        var allCookies = resp.cookies();
                        StringBuilder sb = new StringBuilder();
                        allCookies.forEach((name, values) -> {
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

    private int parseHotScore(String text) {
        if (text == null || text.isEmpty()) return 0;
        Matcher m = HOT_SCORE_PATTERN.matcher(text.replaceAll("[,\\u00a0\\s]", ""));
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
