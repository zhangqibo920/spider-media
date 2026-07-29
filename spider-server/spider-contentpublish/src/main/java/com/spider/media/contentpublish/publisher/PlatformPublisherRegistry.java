package com.spider.media.contentpublish.publisher;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformPublisherRegistry {

    private static final Logger log = LoggerFactory.getLogger(PlatformPublisherRegistry.class);

    private final Map<String, PlatformPublisher> publishers = new HashMap<>();
    private final List<PlatformPublisher> publisherList;
    private PlatformPublisher fallback;

    public PlatformPublisherRegistry(List<PlatformPublisher> publisherList) {
        this.publisherList = publisherList;
    }

    @PostConstruct
    public void init() {
        for (PlatformPublisher p : publisherList) {
            if ("*".equals(p.platform())) {
                fallback = p;
            } else {
                publishers.put(p.platform(), p);
                log.info("注册平台发布器: {}", p.platform());
            }
        }
        if (fallback == null) {
            log.warn("未找到模拟发布器，发布不可用");
        }
    }

    public PlatformPublisher getPublisher(String platform) {
        return publishers.getOrDefault(platform, fallback);
    }
}
