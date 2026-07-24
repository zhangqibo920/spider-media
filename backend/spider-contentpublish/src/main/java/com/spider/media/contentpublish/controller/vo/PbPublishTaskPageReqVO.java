package com.spider.media.contentpublish.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发布任务分页请求 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PbPublishTaskPageReqVO extends PageParam {

    /** 用户ID */
    private Long userId;

    /** 平台类型 */
    private String platform;

    /** 状态（0草稿 1发布中 2已发布 3失败） */
    private Integer status;
}
