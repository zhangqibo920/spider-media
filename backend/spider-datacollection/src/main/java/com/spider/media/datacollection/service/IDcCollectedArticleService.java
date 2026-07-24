package com.spider.media.datacollection.service;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.datacollection.controller.vo.DcCollectedArticlePageReqVO;
import com.spider.media.datacollection.entity.DcCollectedArticle;

import java.util.List;

/**
 * 采集文章Service接口
 */
public interface IDcCollectedArticleService {

    /**
     * 查询采集文章分页列表
     */
    PageResult<DcCollectedArticle> selectArticlePage(DcCollectedArticlePageReqVO pageReqVO);

    /**
     * 触发采集任务
     */
    void collectArticles(Long targetAccountId);
}
