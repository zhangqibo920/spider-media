package com.spider.media.taskscheduler.job;

import com.spider.media.taskscheduler.entity.TsScheduledTask;
import com.spider.media.taskscheduler.mapper.TsScheduledTaskMapper;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@DisallowConcurrentExecution
@Component
public class QuartzJobDispatcher implements Job {

    private static final Logger log = LoggerFactory.getLogger(QuartzJobDispatcher.class);

    @Autowired
    private TsScheduledTaskMapper taskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long taskId = dataMap.getLong("taskId");
        String taskType = dataMap.getString("taskType");
        String taskName = dataMap.getString("taskName");

        TsScheduledTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 1) {
            log.warn("任务不存在或已停用, taskId={}", taskId);
            return;
        }

        log.info("开始执行定时任务: taskId={}, taskName={}, taskType={}", taskId, taskName, taskType);

        boolean success = false;
        try {
            success = dispatch(taskType, task);
        } catch (Exception e) {
            log.error("定时任务执行异常: taskId={}", taskId, e);
        }

        task.setLastRunTime(LocalDateTime.now());
        task.setNextRunTime(context.getNextFireTime() != null
                ? context.getNextFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                : null);
        task.setRunCount(task.getRunCount() == null ? 1 : task.getRunCount() + 1);
        if (!success) {
            task.setFailCount(task.getFailCount() == null ? 1 : task.getFailCount() + 1);
        }
        taskMapper.updateById(task);

        log.info("定时任务执行完成: taskId={}, success={}", taskId, success);
    }

    private boolean dispatch(String taskType, TsScheduledTask task) {
        log.info("任务调度已触发, taskType={}, config={}", taskType, task.getConfig());
        return true;
    }
}
