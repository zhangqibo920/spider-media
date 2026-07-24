package com.spider.media.datacollection.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 采集文章表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DcCollectedArticle extends BaseEntity {

    /** 文章ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 对标账号ID */
    private Long targetAccountId;

    /** 平台类型 */
    private String platform;

    /** 文章标题 */
    private String title;

    /** 文章内容 */
    private String content;

    /** 摘要 */
    private String summary;

    /** 文章链接 */
    private String url;

    /** 作者 */
    private String author;

    /** 阅读量 */
    private Integer viewCount;

    /** 点赞量 */
    private Integer likeCount;

    /** 评论量 */
    private Integer commentCount;

    /** 分享量 */
    private Integer shareCount;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 采集时间 */
    private LocalDateTime collectedTime;
}
