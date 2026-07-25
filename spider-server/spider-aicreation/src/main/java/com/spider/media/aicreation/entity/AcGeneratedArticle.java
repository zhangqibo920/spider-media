package com.spider.media.aicreation.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 生成文章实体类
 *
 * <p>对应数据库表 ac_generated_article，存储 AI 根据热点话题自动生成的文章内容。
 * 记录生成所用的 AI 模型、文章状态、字数等元信息。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcGeneratedArticle extends BaseEntity {

    /** 文章主键ID */
    private Long id;

    /** 发起生成的用户ID */
    private Long userId;

    /** 关联的热点话题ID */
    private Long hotTopicId;

    /** 文章标题（通常取自热点话题标题） */
    private String title;

    /** 文章正文内容（AI 生成的完整文本） */
    private String content;

    /** 文章摘要（取正文前200字） */
    private String summary;

    /** 使用的 AI 模型标识（如 "deepseek"、"zhipu"） */
    private String modelUsed;

    /** 文章字数（近似值） */
    private Integer wordCount;

    /** 生成状态：GENERATING=生成中，COMPLETED=完成，FAILED=失败 */
    private String status;
}
