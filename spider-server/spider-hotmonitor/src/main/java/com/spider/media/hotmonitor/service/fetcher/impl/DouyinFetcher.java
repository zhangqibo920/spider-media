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
public class DouyinFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(DouyinFetcher.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public DouyinFetcher(WebClient.Builder builder) {
        this.webClient = builder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Accept", "application/json, text/plain, */*")
                .defaultHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .defaultHeader("Referer", "https://www.douyin.com/")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sourceName() {
        return "douyin";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            String json = webClient.get()
                    .uri("https://www.douyin.com/aweme/v1/web/hot/search/list/")
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            if (json != null) {
                JsonNode root = objectMapper.readTree(json);
                int statusCode = root.path("status_code").asInt(-1);
                if (statusCode != 0) return topics;
                JsonNode data = root.path("data").path("word_list");
                if (data.isArray()) {
                    int rank = 1;
                    for (JsonNode item : data) {
                        String title = item.path("word").asText("");
                        if (keyword == null || keyword.isEmpty() || title.contains(keyword)) {
                            AcHotTopic topic = new AcHotTopic();
                            topic.setTitle(title);
                            topic.setDescription(item.path("word_sub_board").asText(""));
                            topic.setHotScore(item.path("hot_value").asInt(0));
                            topic.setUrl("https://www.douyin.com/search/" + title);
                            topic.setCategory("抖音热搜");
                            topics.add(topic);
                        }
                        if (++rank > 30) break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("抖音热搜抓取失败", e);
        }
        return topics;
    }
}
