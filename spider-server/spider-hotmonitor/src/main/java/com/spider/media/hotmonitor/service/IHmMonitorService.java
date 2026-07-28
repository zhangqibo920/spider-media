package com.spider.media.hotmonitor.service;

import com.spider.media.aicreation.entity.AcHotTopic;

import java.util.List;

public interface IHmMonitorService {

    void scanAndExecute();

    List<AcHotTopic> fetchByKeyword(Long keywordId, Long userId);

    List<AcHotTopic> queryTopics(Long userId, String keyword, String source,
                                 Integer minScore, Integer minRelevance,
                                 String sortBy, String sortOrder);
}
