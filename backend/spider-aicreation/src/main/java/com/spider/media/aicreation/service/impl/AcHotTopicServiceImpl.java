package com.spider.media.aicreation.service.impl;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.aicreation.mapper.AcHotTopicMapper;
import com.spider.media.aicreation.service.IAcHotTopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 热点话题Service实现
 */
@Service
public class AcHotTopicServiceImpl implements IAcHotTopicService {

    private static final Logger log = LoggerFactory.getLogger(AcHotTopicServiceImpl.class);

    private final AcHotTopicMapper hotTopicMapper;

    public AcHotTopicServiceImpl(AcHotTopicMapper hotTopicMapper) {
        this.hotTopicMapper = hotTopicMapper;
    }

    @Override
    @Async
    public void fetchHotTopics(String platform, Long userId) {
        log.info("开始抓取热点话题, platform={}, userId={}", platform, userId);
        // TODO: 实现实际的热点抓取逻辑
    }

    @Override
    public List<AcHotTopic> selectHotTopicList(Long userId) {
        return hotTopicMapper.selectByUserId(userId);
    }
}
