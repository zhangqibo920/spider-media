package com.spider.media.hotmonitor.service.fetcher.impl;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.service.fetcher.IHotSourceFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BaiduFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(BaiduFetcher.class);

    @Override
    public String sourceName() {
        return "baidu";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://top.baidu.com/board?tab=realtime")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();
            Elements items = doc.select("#sanRoot .content_1YWBm .category-wrap_iQLoo .content-wrapper .content_28Njm");
            int rank = 1;
            for (Element item : items) {
                String title = item.select(".c-single-text-ellipsis").text();
                String desc = item.select(".desc_3CTjT").text();
                String hot = item.select(".hot-index_1Bl1a").text();
                if (title.isEmpty()) continue;
                if (keyword == null || keyword.isEmpty() || title.contains(keyword)) {
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(title);
                    topic.setDescription(desc);
                    topic.setHotScore(parseHot(hot));
                    topic.setUrl("https://top.baidu.com/board?tab=realtime");
                    topic.setCategory("百度热搜");
                    topics.add(topic);
                }
                if (++rank > 30) break;
            }
        } catch (Exception e) {
            log.warn("百度热搜抓取失败", e);
        }
        return topics;
    }

    private int parseHot(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
