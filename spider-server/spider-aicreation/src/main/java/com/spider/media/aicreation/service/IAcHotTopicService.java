package com.spider.media.aicreation.service;

import com.spider.media.aicreation.entity.AcHotTopic;

import java.util.List;

/**
 * 热点话题业务层接口
 *
 * <p>定义热点话题抓取和查询的核心业务方法。
 * 由 {@link com.spider.media.aicreation.service.impl.AcHotTopicServiceImpl} 提供具体实现。</p>
 */
public interface IAcHotTopicService {

    /**
     * 从指定平台异步抓取热点话题并保存到数据库
     *
     * <p>支持的平台：weibo（微博）、douyin（抖音）、zhihu（知乎）、toutiao（头条）。
     * 抓取前会先删除该用户在该平台的旧热点数据，再保存新抓取的数据。</p>
     *
     * @param platform 平台类型
     * @param userId   用户ID
     */
    void fetchHotTopics(String platform, Long userId);

    /**
     * 查询指定用户的所有热点话题列表
     *
     * @param userId 用户ID
     * @return 热点话题列表
     */
    List<AcHotTopic> selectHotTopicList(Long userId);
}
