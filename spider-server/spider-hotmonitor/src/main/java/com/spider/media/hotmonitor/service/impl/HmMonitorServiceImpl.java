package com.spider.media.hotmonitor.service.impl;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.aicreation.mapper.AcHotTopicMapper;
import com.spider.media.hotmonitor.entity.HmKeyword;
import com.spider.media.hotmonitor.entity.HmNotification;
import com.spider.media.hotmonitor.mapper.HmKeywordMapper;
import com.spider.media.hotmonitor.mapper.HmNotificationMapper;
import com.spider.media.hotmonitor.ai.HmAiAnalyzer;
import com.spider.media.hotmonitor.ai.dto.AnalysisResult;
import com.spider.media.hotmonitor.service.IHmMonitorService;
import com.spider.media.hotmonitor.service.fetcher.IHotSourceFetcher;
import com.spider.media.hotmonitor.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class HmMonitorServiceImpl implements IHmMonitorService {

    private static final Logger log = LoggerFactory.getLogger(HmMonitorServiceImpl.class);

    private final HmKeywordMapper keywordMapper;
    private final AcHotTopicMapper hotTopicMapper;
    private final HmNotificationMapper notificationMapper;
    private final List<IHotSourceFetcher> fetchers;
    private final HmAiAnalyzer aiAnalyzer;

    public HmMonitorServiceImpl(HmKeywordMapper keywordMapper,
                                 AcHotTopicMapper hotTopicMapper,
                                 HmNotificationMapper notificationMapper,
                                 List<IHotSourceFetcher> fetchers,
                                 HmAiAnalyzer aiAnalyzer) {
        this.keywordMapper = keywordMapper;
        this.hotTopicMapper = hotTopicMapper;
        this.notificationMapper = notificationMapper;
        this.fetchers = fetchers;
        this.aiAnalyzer = aiAnalyzer;
    }

    @Scheduled(fixedDelay = 60_000)
    @Override
    public void scanAndExecute() {
        LocalDateTime now = LocalDateTime.now();
        List<HmKeyword> dueKeywords = keywordMapper.selectActiveKeywords(now);
        for (HmKeyword keyword : dueKeywords) {
            try {
                executeKeyword(keyword, now);
            } catch (Exception e) {
                log.error("关键词监控执行失败, keywordId={}, keyword={}", keyword.getId(), keyword.getKeyword(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeKeyword(HmKeyword keyword, LocalDateTime now) {
        log.info("开始监控关键词: {}", keyword.getKeyword());
        List<AcHotTopic> allTopics = new ArrayList<>();

        for (IHotSourceFetcher fetcher : fetchers) {
            try {
                List<AcHotTopic> topics = fetcher.fetch(keyword.getKeyword());
                for (AcHotTopic topic : topics) {
                    topic.setUserId(keyword.getUserId());
                    topic.setKeywordId(keyword.getId());
                    topic.setSource(fetcher.sourceName());
                    topic.setPlatform(fetcher.sourceName());
                }
                allTopics.addAll(topics);
            } catch (Exception e) {
                log.warn("来源抓取失败: {}", fetcher.sourceName(), e);
            }
        }

        if (!allTopics.isEmpty()) {
            int highImportanceCount = 0;
            for (AcHotTopic topic : allTopics) {
                AnalysisResult analysis = aiAnalyzer.analyze(
                        keyword.getKeyword(), topic.getTitle(), topic.getDescription());
                topic.setRelevance(analysis.getRelevance());
                topic.setAiSummary(analysis.getSummary());
                topic.setAiScore(analysis.getImportance());
                topic.setAiVerified(analysis.getIsFake());
                topic.setCreateBy("system");
                topic.setCreateTime(LocalDateTime.now());
                hotTopicMapper.insert(topic);
                if (analysis.getImportance() >= 4) highImportanceCount++;
            }

            if ("1".equals(keyword.getNotifySite())) {
                AcHotTopic top = allTopics.stream()
                        .max(Comparator.comparingInt(AcHotTopic::getHotScore))
                        .orElse(allTopics.get(0));
                HmNotification notification = new HmNotification();
                notification.setUserId(keyword.getUserId());
                notification.setTitle("热点监控: " + keyword.getKeyword());
                notification.setContent("发现 " + allTopics.size() + " 条相关热点，最高热度: " + top.getTitle());
                notification.setType("HOT");
                notification.setHotTopicId(top.getId());
                notification.setCreateBy("system");
                notification.setCreateTime(LocalDateTime.now());
                notificationMapper.insert(notification);

                String wsMsg = "{\"type\":\"notification\",\"unreadCount\":1,\"title\":\"" +
                        notification.getTitle() + "\",\"content\":\"" +
                        notification.getContent() + "\",\"time\":\"" +
                        notification.getCreateTime() + "\"}";
                WebSocketSessionManager.sendToUser(keyword.getUserId(), wsMsg);
            }
        }

        keywordMapper.updateLastFetchTime(keyword.getId(), now);
        log.info("关键词监控完成: {}, 获取{}条", keyword.getKeyword(), allTopics.size());
    }

    @Override
    public List<AcHotTopic> fetchByKeyword(Long keywordId, Long userId) {
        return hotTopicMapper.selectByKeywordId(keywordId, userId);
    }

    @Override
    public List<AcHotTopic> queryTopics(Long userId, String keyword, String source,
                                         Integer minScore, Integer minRelevance,
                                         String sortBy, String sortOrder) {
        List<AcHotTopic> list = hotTopicMapper.selectByFilter(userId, keyword, source,
                minScore, minRelevance, sortBy, sortOrder);
        return list;
    }
}
