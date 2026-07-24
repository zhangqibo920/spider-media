package com.spider.media.aicreation.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热点话题表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcHotTopic extends BaseEntity {

    /** 话题ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 平台类型 */
    private String platform;

    /** 话题标题 */
    private String title;

    /** 话题描述 */
    private String description;

    /** 热度值 */
    private Integer hotScore;

    /** 链接 */
    private String url;

    /** 分类 */
    private String category;
}
