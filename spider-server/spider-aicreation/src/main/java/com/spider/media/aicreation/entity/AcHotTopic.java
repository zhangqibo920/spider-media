package com.spider.media.aicreation.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热点话题实体类
 *
 * <p>对应数据库表 ac_hot_topic，存储从各平台（微博、抖音、知乎、头条）抓取的热点话题数据。
 * 包含话题标题、描述、热度值、原始链接等信息，用于 AI 文章生成的素材。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcHotTopic extends BaseEntity {

    /** 话题主键ID */
    private Long id;

    /** 所属用户ID（每个用户独立管理自己的热点列表） */
    private Long userId;

    /** 来源平台（如 "weibo"、"douyin"、"zhihu"、"toutiao"） */
    private String platform;

    /** 话题标题 */
    private String title;

    /** 话题描述/摘要 */
    private String description;

    /** 热度值（数值越大越热门） */
    private Integer hotScore;

    /** 话题原始链接（指向平台的热搜页面） */
    private String url;

    /** 话题分类（如 "微博热搜"、"知乎热榜"） */
    private String category;

    /** 关联关键词ID（热点监控模块使用） */
    private Long keywordId;

    /** 来源标识（如 weibo/douyin/zhihu/toutiao） */
    private String source;

    /** AI 重要性评分 1-5 */
    private Integer aiScore;

    /** AI 智能摘要 */
    private String aiSummary;

    /** AI 真假判定 0=未验证 1=真实 2=可疑 3=虚假 */
    private String aiVerified;

    /** 与关键词相关性 0-100 */
    private Integer relevance;
}
