package com.spider.media.datacollection.service.impl;

import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.datacollection.controller.vo.DcCollectedArticlePageReqVO;
import com.spider.media.datacollection.entity.DcCollectedArticle;
import com.spider.media.datacollection.mapper.DcCollectedArticleMapper;
import com.spider.media.datacollection.service.IDcCollectedArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 采集文章Service实现
 */
@Service
public class DcCollectedArticleServiceImpl implements IDcCollectedArticleService {

    private static final Logger log = LoggerFactory.getLogger(DcCollectedArticleServiceImpl.class);

    private final DcCollectedArticleMapper collectedArticleMapper;

    public DcCollectedArticleServiceImpl(DcCollectedArticleMapper collectedArticleMapper) {
        this.collectedArticleMapper = collectedArticleMapper;
    }

    @Override
    public PageResult<DcCollectedArticle> selectArticlePage(DcCollectedArticlePageReqVO pageReqVO) {
        return PageUtils.selectPage(pageReqVO, () ->
                collectedArticleMapper.selectPage(
                        pageReqVO.getTargetAccountId(),
                        pageReqVO.getPlatform(),
                        pageReqVO.getTitle()
                )
        );
    }

    @Override
    @Async
    public void collectArticles(Long targetAccountId) {
        log.info("开始采集对标账号数据, targetAccountId={}", targetAccountId);
        // TODO: 实现实际的数据采集逻辑
    }
}
