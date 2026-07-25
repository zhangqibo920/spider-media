package com.spider.media.contentpublish.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 发布任务实体类
 *
 * <p>对应数据库表 pb_publish_task，存储内容发布任务的完整生命周期信息。
 * 包含任务状态流转（草稿 → 发布中 → 已发布/失败）、定时发布、重试机制等。</p>
 *
 * <p>状态说明：
 * <ul>
 *   <li>0 = 草稿（刚创建，未发布）</li>
 *   <li>1 = 发布中（正在调用平台 API）</li>
 *   <li>2 = 已发布（成功发布到平台）</li>
 *   <li>3 = 失败（发布失败，可重试）</li>
 * </ul></p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PbPublishTask extends BaseEntity {

    /** 任务主键ID */
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 关联的发布平台账号ID */
    private Long platformAccountId;

    /** 关联的 AI 生成文章ID */
    private Long articleId;

    /** 目标发布平台（如 "douyin"、"kuaishou"、"xiaohongshu"） */
    private String platform;

    /** 发布标题 */
    private String title;

    /** 发布正文内容 */
    private String content;

    /** 内容摘要 */
    private String summary;

    /** 封面图片 URL */
    private String coverImage;

    /** 任务状态：0=草稿，1=发布中，2=已发布，3=失败 */
    private Integer status;

    /** 定时发布时间（为 null 表示立即发布） */
    private LocalDateTime scheduledTime;

    /** 实际发布时间（发布成功后填充） */
    private LocalDateTime publishedTime;

    /** 发布结果描述（成功/失败信息） */
    private String publishResult;

    /** 已重试次数 */
    private Integer retryCount;
}
