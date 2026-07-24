package com.spider.media.taskscheduler.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务分页请求 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TsScheduledTaskPageReqVO extends PageParam {

    /** 用户ID */
    private Long userId;

    /** 任务名称（模糊查询） */
    private String taskName;

    /** 状态（0停止 1运行中） */
    private Integer status;
}
