package com.spider.media.datacollection.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采集文章分页请求 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DcCollectedArticlePageReqVO extends PageParam {

    /** 对标账号ID */
    private Long targetAccountId;

    /** 平台类型 */
    private String platform;

    /** 标题（模糊查询） */
    private String title;
}
