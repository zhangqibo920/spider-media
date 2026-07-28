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
public class HackerNewsFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(HackerNewsFetcher.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public HackerNewsFetcher(WebClient.Builder builder) {
        this.webClient = builder
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sourceName() {
        return "hackernews";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
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
                if (keyword == null || keyword.isEmpty() || title.toLowerCase().contains(keyword.toLowerCase())) {
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(title);
                    topic.setDescription(item.path("text").asText(""));
                    topic.setHotScore(item.path("score").asInt(0));
                    topic.setUrl(item.path("url").asText("https://news.ycombinator.com/item?id=" + id));
                    topic.setCategory("HackerNews");
                    topics.add(topic);
                    count++;
                }
            }
        } catch (Exception e) {
            log.warn("HackerNews抓取失败", e);
        }
        return topics;
    }
}
