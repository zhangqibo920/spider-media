package com.spider.media.aicreation.service;

import com.spider.media.aicreation.controller.vo.AcGeneratedArticlePageReqVO;
import com.spider.media.aicreation.entity.AcGeneratedArticle;
import com.spider.media.common.pojo.PageResult;

import java.util.List;

/**
 * AI生成文章Service接口
 */
public interface IAcGeneratedArticleService {

    /**
     * 生成文章
     */
    AcGeneratedArticle generateArticle(Long hotTopicId, Long userId, String model);

    /**
     * 查询文章分页列表
     */
    PageResult<AcGeneratedArticle> selectArticlePage(AcGeneratedArticlePageReqVO pageReqVO);
}
