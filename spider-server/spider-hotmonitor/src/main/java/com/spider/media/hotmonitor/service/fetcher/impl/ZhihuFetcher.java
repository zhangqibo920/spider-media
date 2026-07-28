package com.spider.media.hotmonitor.service.fetcher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.service.fetcher.IHotSourceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class ZhihuFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(ZhihuFetcher.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ZhihuFetcher(WebClient.Builder builder) {
        this.webClient = builder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Accept", "application/json, text/plain, */*")
                .defaultHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sourceName() {
        return "zhihu";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=30")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
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
        } catch (Exception e) {
            log.warn("知乎热榜抓取失败", e);
        }
        return topics;
    }
}
