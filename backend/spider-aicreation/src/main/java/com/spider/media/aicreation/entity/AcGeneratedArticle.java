package com.spider.media.aicreation.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI生成文章表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcGeneratedArticle extends BaseEntity {

    /** 文章ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 热点话题ID */
    private Long hotTopicId;

    /** 文章标题 */
    private String title;

    /** 文章内容 */
    private String content;

    /** 摘要 */
    private String summary;

    /** 使用的模型 */
    private String modelUsed;

    /** 字数 */
    private Integer wordCount;

    /** 状态（GENERATING生成中 COMPLETED完成 FAILED失败） */
    private String status;
}
