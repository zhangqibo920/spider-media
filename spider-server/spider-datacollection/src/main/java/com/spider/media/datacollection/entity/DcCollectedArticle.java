package com.spider.media.datacollection.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 采集文章实体类
 *
 * <p>对应数据库表 dc_collected_article，存储从对标账号采集到的文章数据。
 * 包含文章的基本信息（标题、内容、链接）和互动数据（阅读、点赞、评论、分享）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DcCollectedArticle extends BaseEntity {

    /** 文章主键ID */
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 来源对标账号ID */
    private Long targetAccountId;

    /** 来源平台类型 */
    private String platform;

    /** 文章标题 */
    private String title;

    /** 文章正文内容 */
    private String content;

    /** 文章摘要（取正文前200字） */
    private String summary;

    /** 文章原始链接 */
    private String url;

    /** 文章作者（即对标账号名称） */
    private String author;

    /** 阅读量 */
    private Integer viewCount;

    /** 点赞量 */
    private Integer likeCount;

    /** 评论量 */
    private Integer commentCount;

    /** 分享量 */
    private Integer shareCount;

    /** 文章在平台上的发布时间 */
    private LocalDateTime publishTime;

    /** 文章被采集到系统的时间 */
    private LocalDateTime collectedTime;
}
