package com.spider.media.hotmonitor.service.fetcher.impl;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.service.fetcher.IHotSourceFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeiboFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(WeiboFetcher.class);

    @Override
    public String sourceName() {
        return "weibo";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://s.weibo.com/top/summary")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            int rank = 1;
            for (Element tr : doc.select("#pl_top_realtimehot table tbody tr")) {
                String title = tr.select(".td-02 a").text();
                String hot = tr.select(".td-02 span").text();
                if (title.isEmpty()) continue;
                if (keyword == null || keyword.isEmpty() || title.contains(keyword)) {
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(title);
                    topic.setDescription("");
                    topic.setHotScore(parseHot(hot));
                    topic.setUrl("https://s.weibo.com" + tr.select(".td-02 a").attr("href"));
                    topic.setCategory("微博热搜");
                    topics.add(topic);
                }
                if (++rank > 30) break;
            }
        } catch (Exception e) {
            log.warn("微博热搜抓取失败", e);
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
