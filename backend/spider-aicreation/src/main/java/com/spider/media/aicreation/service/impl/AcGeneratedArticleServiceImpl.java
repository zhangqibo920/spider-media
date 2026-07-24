package com.spider.media.aicreation.service.impl;

import com.spider.media.aicreation.controller.vo.AcGeneratedArticlePageReqVO;
import com.spider.media.aicreation.entity.AcGeneratedArticle;
import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.aicreation.mapper.AcGeneratedArticleMapper;
import com.spider.media.aicreation.mapper.AcHotTopicMapper;
import com.spider.media.aicreation.service.IAcGeneratedArticleService;
import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.ErrorCodeEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI生成文章Service实现
 */
@Service
public class AcGeneratedArticleServiceImpl implements IAcGeneratedArticleService {

    private static final Logger log = LoggerFactory.getLogger(AcGeneratedArticleServiceImpl.class);

    private final AcGeneratedArticleMapper generatedArticleMapper;
    private final AcHotTopicMapper hotTopicMapper;

    public AcGeneratedArticleServiceImpl(AcGeneratedArticleMapper generatedArticleMapper,
                                          AcHotTopicMapper hotTopicMapper) {
        this.generatedArticleMapper = generatedArticleMapper;
        this.hotTopicMapper = hotTopicMapper;
    }

    @Override
    public AcGeneratedArticle generateArticle(Long hotTopicId, Long userId, String model) {
        AcHotTopic hotTopic = hotTopicMapper.selectById(hotTopicId);
        if (hotTopic == null) {
            throw new ServiceException(ErrorCodeEnums.AC_HOT_TOPIC_NOT_FOUND);
        }

        AcGeneratedArticle article = new AcGeneratedArticle();
        article.setUserId(userId);
        article.setHotTopicId(hotTopicId);
        article.setModelUsed(model);
        article.setStatus("GENERATING");
        article.setCreateBy(String.valueOf(userId));
        article.setCreateTime(LocalDateTime.now());

        // TODO: 实现实际的AI生成逻辑
        log.info("开始生成文章, topic={}, model={}", hotTopic.getTitle(), model);

        generatedArticleMapper.insert(article);
        return article;
    }

    @Override
    public PageResult<AcGeneratedArticle> selectArticlePage(AcGeneratedArticlePageReqVO pageReqVO) {
        return PageUtils.selectPage(pageReqVO, () ->
                generatedArticleMapper.selectPage(
                        pageReqVO.getUserId(),
                        pageReqVO.getStatus(),
                        pageReqVO.getTitle()
                )
        );
    }
}
