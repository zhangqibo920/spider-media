package com.spider.media.taskscheduler.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务分页查询请求 VO
 *
 * <p>继承 PageParam 获得分页参数，额外支持按用户ID、任务名、状态进行筛选查询。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TsScheduledTaskPageReqVO extends PageParam {

    /** 用户ID（筛选条件） */
    private Long userId;

    /** 任务名称模糊查询关键字 */
    private String taskName;

    /** 任务状态筛选：0=停止，1=运行中 */
    private Integer status;
}
