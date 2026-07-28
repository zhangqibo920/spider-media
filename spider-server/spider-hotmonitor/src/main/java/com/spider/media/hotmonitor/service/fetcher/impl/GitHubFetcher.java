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
public class GitHubFetcher implements IHotSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(GitHubFetcher.class);

    @Override
    public String sourceName() {
        return "github";
    }

    @Override
    public List<AcHotTopic> fetch(String keyword) {
        List<AcHotTopic> topics = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://github.com/trending")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();
            Elements articles = doc.select("article.Box-row");
            int rank = 1;
            for (Element article : articles) {
                String repo = article.select("h2 a").text().replaceAll("\\s+", "");
                String desc = article.select("p").text();
                String stars = article.select(".d-inline-block.float-sm-right").text();
                if (repo.isEmpty()) continue;
                if (keyword == null || keyword.isEmpty() || repo.toLowerCase().contains(keyword.toLowerCase()) || desc.toLowerCase().contains(keyword.toLowerCase())) {
                    AcHotTopic topic = new AcHotTopic();
                    topic.setTitle(repo);
                    topic.setDescription(desc);
                    topic.setHotScore(parseStars(stars));
                    topic.setUrl("https://github.com/" + repo);
                    topic.setCategory("GitHub Trending");
                    topics.add(topic);
                }
                if (++rank > 30) break;
            }
        } catch (Exception e) {
            log.warn("GitHub Trending抓取失败", e);
        }
        return topics;
    }

    private int parseStars(String text) {
        try {
            String s = text.replaceAll("[^0-9kK]", "").replaceAll("(?i)k", "000");
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
