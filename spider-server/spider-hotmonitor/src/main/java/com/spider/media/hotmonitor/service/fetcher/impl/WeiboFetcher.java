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
public class WeiboFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(WeiboFetcher.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public WeiboFetcher(WebClient.Builder builder) {
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
        return "weibo";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://weibo.com/ajax/side/hotSearch")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data").path("realtime");
                if (data.isArray()) {
                    int rank = 1;
                    for (JsonNode item : data) {
                        String title = item.path("note").asText("");
                        if (keyword == null || keyword.isEmpty() || title.contains(keyword)) {
                            AcHotTopic topic = new AcHotTopic();
                            topic.setTitle(title);
                            topic.setDescription(item.path("label_name").asText(""));
                            topic.setHotScore(item.path("num").asInt(0));
                            topic.setUrl("https://s.weibo.com/weibo?q=%23" + item.path("word").asText("") + "%23");
                            topic.setCategory("微博热搜");
                            topics.add(topic);
                        }
                        if (++rank > 30) break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("微博热搜抓取失败", e);
        }
        return topics;
    }
}
