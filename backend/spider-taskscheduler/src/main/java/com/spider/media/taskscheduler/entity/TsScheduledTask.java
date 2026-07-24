package com.spider.media.taskscheduler.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 定时任务表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TsScheduledTask extends BaseEntity {

    /** 任务ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 任务名称 */
    private String taskName;

    /** 任务类型 */
    private String taskType;

    /** Cron表达式 */
    private String cronExpression;

    /** 状态（0停止 1运行中） */
    private Integer status;

    /** 上次执行时间 */
    private LocalDateTime lastRunTime;

    /** 下次执行时间 */
    private LocalDateTime nextRunTime;

    /** 执行次数 */
    private Integer runCount;

    /** 失败次数 */
    private Integer failCount;

    /** 配置参数 */
    private String config;
}
