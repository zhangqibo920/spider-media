package com.spider.media.datacollection.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采集文章分页查询请求 VO
 *
 * <p>继承 PageParam 获得分页参数，额外支持按对标账号ID、平台、标题进行筛选查询。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DcCollectedArticlePageReqVO extends PageParam {

    /** 对标账号ID（筛选条件） */
    private Long targetAccountId;

    /** 平台类型筛选 */
    private String platform;

    /** 标题模糊查询关键字 */
    private String title;
}
