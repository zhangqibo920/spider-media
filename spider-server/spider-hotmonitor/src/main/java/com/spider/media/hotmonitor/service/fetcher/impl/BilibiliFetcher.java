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
public class BilibiliFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(BilibiliFetcher.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public BilibiliFetcher(WebClient.Builder builder) {
        this.webClient = builder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Referer", "https://www.bilibili.com/")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sourceName() {
        return "bilibili";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
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
                    int rank = 1;
                    for (JsonNode item : data) {
                        String title = item.path("title").asText("");
                        if (keyword == null || keyword.isEmpty() || title.contains(keyword)) {
                            AcHotTopic topic = new AcHotTopic();
                            topic.setTitle(title);
                            topic.setDescription(item.path("desc").asText(""));
                            topic.setHotScore(item.path("stat").path("view").asInt(0));
                            topic.setUrl("https://www.bilibili.com/video/" + item.path("bvid").asText());
                            topic.setCategory("B站热门");
                            topics.add(topic);
                        }
                        if (++rank > 30) break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("B站热门抓取失败", e);
        }
        return topics;
    }
}
