package com.spider.media.contentpublish.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 发布任务表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PbPublishTask extends BaseEntity {

    /** 任务ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 平台账号ID */
    private Long platformAccountId;

    /** 文章ID */
    private Long articleId;

    /** 平台类型 */
    private String platform;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 摘要 */
    private String summary;

    /** 封面图片 */
    private String coverImage;

    /** 状态（0草稿 1发布中 2已发布 3定时中 4失败） */
    private Integer status;

    /** 定时发布时间 */
    private LocalDateTime scheduledTime;

    /** 实际发布时间 */
    private LocalDateTime publishedTime;

    /** 发布结果 */
    private String publishResult;

    /** 重试次数 */
    private Integer retryCount;
}
