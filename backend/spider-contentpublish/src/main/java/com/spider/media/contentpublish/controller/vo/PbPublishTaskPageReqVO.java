package com.spider.media.contentpublish.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发布任务分页查询请求 VO
 *
 * <p>继承 PageParam 获得分页参数，额外支持按用户ID、平台、状态进行筛选查询。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PbPublishTaskPageReqVO extends PageParam {

    /** 用户ID（筛选条件） */
    private Long userId;

    /** 平台类型筛选 */
    private String platform;

    /** 任务状态筛选：0=草稿，1=发布中，2=已发布，3=失败 */
    private Integer status;
}
