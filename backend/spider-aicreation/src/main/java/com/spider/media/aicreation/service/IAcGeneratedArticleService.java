package com.spider.media.aicreation.service;

import com.spider.media.aicreation.controller.vo.AcGeneratedArticlePageReqVO;
import com.spider.media.aicreation.entity.AcGeneratedArticle;
import com.spider.media.common.pojo.PageResult;

import java.util.List;

/**
 * AI 生成文章业务层接口
 *
 * <p>定义文章生成和查询的核心业务方法。
 * 由 {@link com.spider.media.aicreation.service.impl.AcGeneratedArticleServiceImpl} 提供具体实现。</p>
 */
public interface IAcGeneratedArticleService {

    /**
     * 根据热点话题生成 AI 文章
     *
     * @param hotTopicId 热点话题ID
     * @param userId     用户ID
     * @param model      AI 模型标识（如 "deepseek"、"zhipu"）
     * @return 生成的文章实体（状态可能为 COMPLETED 或 FAILED）
     */
    AcGeneratedArticle generateArticle(Long hotTopicId, Long userId, String model);

    /**
     * 分页查询 AI 生成文章列表
     *
     * @param pageReqVO 分页查询参数（用户ID、状态、标题）
     * @return 分页结果
     */
    PageResult<AcGeneratedArticle> selectArticlePage(AcGeneratedArticlePageReqVO pageReqVO);
}
