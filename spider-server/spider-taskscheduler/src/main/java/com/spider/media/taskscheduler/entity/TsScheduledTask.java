package com.spider.media.taskscheduler.entity;

import com.spider.media.common.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 定时任务实体类
 *
 * <p>对应数据库表 ts_scheduled_task，存储用户创建的定时任务配置。
 * 支持 Cron 表达式配置执行计划，记录任务的运行状态和执行统计。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TsScheduledTask extends BaseEntity {

    /** 任务主键ID */
    private Long id;

    /** 所属用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 任务名称（便于用户识别） */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100")
    private String taskName;

    /** 任务类型（如 "热点抓取"、"文章生成"、"内容发布"） */
    @NotBlank(message = "任务类型不能为空")
    @Size(max = 50, message = "任务类型长度不能超过50")
    private String taskType;

    /** Cron 表达式（定义执行计划，如 "0 0 9 * * ?" 表示每天9点执行） */
    @NotBlank(message = "Cron表达式不能为空")
    @Size(max = 100, message = "Cron表达式长度不能超过100")
    @Pattern(regexp = "^[0-9*/?,-\\s]+$", message = "Cron表达式格式不正确")
    private String cronExpression;

    /** 任务状态：0=已停止，1=运行中 */
    private Integer status;

    /** 上次执行时间 */
    private LocalDateTime lastRunTime;

    /** 下次预计执行时间 */
    private LocalDateTime nextRunTime;

    /** 累计执行次数 */
    private Integer runCount;

    /** 累计失败次数 */
    private Integer failCount;

    /** 任务配置参数（JSON 格式，存储任务特定的配置信息） */
    private String config;
}
