package com.spider.media.aicreation.service;

import com.spider.media.aicreation.entity.AcHotTopic;

import java.util.List;

/**
 * 热点话题Service接口
 */
public interface IAcHotTopicService {

    /**
     * 抓取热点话题
     */
    void fetchHotTopics(String platform, Long userId);

    /**
     * 查询热点列表
     */
    List<AcHotTopic> selectHotTopicList(Long userId);
}
