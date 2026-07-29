package com.spider.media.datacollection.service;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.datacollection.controller.vo.DcCollectedArticlePageReqVO;
import com.spider.media.datacollection.entity.DcCollectedArticle;

import java.util.List;

/**
 * 采集文章业务层接口
 *
 * <p>定义采集文章的查询和触发采集任务的操作。
 * 由 {@link com.spider.media.datacollection.service.impl.DcCollectedArticleServiceImpl} 提供具体实现。</p>
 */
public interface IDcCollectedArticleService {

    /**
     * 分页查询采集文章列表
     *
     * @param pageReqVO 分页查询参数（对标账号ID、平台、标题）
     * @return 文章分页结果
     */
    PageResult<DcCollectedArticle> selectArticlePage(DcCollectedArticlePageReqVO pageReqVO);

    /**
     * 触发对标账号的文章采集任务（异步执行）
     *
     * <p>根据对标账号的主页链接，抓取文章列表并保存到数据库。
     * 通过 URL 去重避免重复采集同一篇文章。</p>
     *
     * <p>采集前会校验账号归属，仅允许账号所有者触发采集，防止越权操作。</p>
     *
     * @param targetAccountId 对标账号ID
     * @param operatorId      当前操作用户ID（用于归属校验）
     * @throws com.spider.media.common.exception.ServiceException 账号不存在或不属于当前用户时抛出
     */
    void collectArticles(Long targetAccountId, Long operatorId);

    /**
     * 逻辑删除单篇采集文章
     *
     * @param id      文章ID
     * @param userId  当前操作用户ID
     */
    void deleteArticleById(Long id, Long userId);
}
