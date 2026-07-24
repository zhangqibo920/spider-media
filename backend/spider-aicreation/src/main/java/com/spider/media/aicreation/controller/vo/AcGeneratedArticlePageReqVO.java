package com.spider.media.aicreation.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI生成文章分页请求 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcGeneratedArticlePageReqVO extends PageParam {

    /** 用户ID */
    private Long userId;

    /** 状态（GENERATING/COMPLETED/FAILED） */
    private String status;

    /** 标题（模糊查询） */
    private String title;
}
