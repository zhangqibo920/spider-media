package com.spider.media.taskscheduler.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.taskscheduler.controller.vo.TsScheduledTaskPageReqVO;
import com.spider.media.taskscheduler.entity.TsScheduledTask;
import com.spider.media.taskscheduler.job.QuartzJobDispatcher;
import com.spider.media.taskscheduler.mapper.TsScheduledTaskMapper;
import com.spider.media.taskscheduler.service.ITsScheduledTaskService;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TsScheduledTaskServiceImpl implements ITsScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(TsScheduledTaskServiceImpl.class);
    private static final String JOB_GROUP = "SPIDER_MEDIA_GROUP";

    private final TsScheduledTaskMapper scheduledTaskMapper;
    private final Scheduler scheduler;

    public TsScheduledTaskServiceImpl(TsScheduledTaskMapper scheduledTaskMapper, Scheduler scheduler) {
        this.scheduledTaskMapper = scheduledTaskMapper;
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void reloadActiveTasks() {
        List<TsScheduledTask> activeTasks = scheduledTaskMapper.selectPage(null, null, 1);
        for (TsScheduledTask task : activeTasks) {
            try {
                scheduleJob(task);
                log.info("恢复定时任务: taskId={}, taskName={}", task.getId(), task.getTaskName());
            } catch (SchedulerException e) {
                log.error("恢复定时任务失败: taskId={}", task.getId(), e);
            }
        }
        log.info("定时任务恢复完成, 共恢复{}个任务", activeTasks.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TsScheduledTask createTask(TsScheduledTask task) {
        task.setStatus(0);
        task.setRunCount(0);
        task.setFailCount(0);
        task.setCreateBy(String.valueOf(task.getUserId()));
        task.setCreateTime(LocalDateTime.now());
        scheduledTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableTask(Long taskId) {
        TsScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_NOT_FOUND);
        }
        try {
            scheduleJob(task);
            task.setStatus(1);
            task.setUpdateBy(LoginUser.getUsername());
            task.setUpdateTime(LocalDateTime.now());
            scheduledTaskMapper.updateById(task);
            log.info("启用任务: {}", task.getTaskName());
        } catch (SchedulerException e) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_ENABLE_FAILED, "启用定时任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableTask(Long taskId) {
        TsScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_NOT_FOUND);
        }
        try {
            unscheduleJob(task.getId());
            task.setStatus(0);
            task.setUpdateBy(LoginUser.getUsername());
            task.setUpdateTime(LocalDateTime.now());
            scheduledTaskMapper.updateById(task);
            log.info("停用任务: {}", task.getTaskName());
        } catch (SchedulerException e) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_DISABLE_FAILED, "停用定时任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(TsScheduledTask task) {
        TsScheduledTask existing = scheduledTaskMapper.selectById(task.getId());
        if (existing == null) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_NOT_FOUND);
        }
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        scheduledTaskMapper.updateById(task);
        if (existing.getStatus() == 1) {
            try {
                unscheduleJob(task.getId());
                scheduleJob(task);
            } catch (SchedulerException e) {
                throw new ServiceException(ErrorCodeEnums.TS_TASK_ENABLE_FAILED, "更新定时任务调度失败: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        TsScheduledTask existing = scheduledTaskMapper.selectById(taskId);
        if (existing == null) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_NOT_FOUND);
        }
        try {
            unscheduleJob(taskId);
        } catch (SchedulerException e) {
            log.error("删除定时任务时取消调度失败: taskId={}", taskId, e);
        }
        scheduledTaskMapper.deleteById(taskId);
    }

    @Override
    public PageResult<TsScheduledTask> selectTaskPage(TsScheduledTaskPageReqVO pageReqVO) {
        return PageUtils.selectPage(pageReqVO, () ->
                scheduledTaskMapper.selectPage(
                        pageReqVO.getUserId(),
                        pageReqVO.getTaskName(),
                        pageReqVO.getStatus()
                )
        );
    }

    private void scheduleJob(TsScheduledTask task) throws SchedulerException {
        JobKey jobKey = jobKey(task.getId());
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        JobDetail jobDetail = JobBuilder.newJob(QuartzJobDispatcher.class)
                .withIdentity(jobKey)
                .usingJobData("taskId", task.getId())
                .usingJobData("taskType", task.getTaskType())
                .usingJobData("taskName", task.getTaskName())
                .storeDurably(false)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(task.getId()))
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    private void unscheduleJob(Long taskId) throws SchedulerException {
        scheduler.unscheduleJob(triggerKey(taskId));
        scheduler.deleteJob(jobKey(taskId));
    }

    private static JobKey jobKey(Long taskId) {
        return JobKey.jobKey("task_" + taskId, JOB_GROUP);
    }

    private static TriggerKey triggerKey(Long taskId) {
        return TriggerKey.triggerKey("trigger_" + taskId, JOB_GROUP);
    }
}
