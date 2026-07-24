package com.spider.media.taskscheduler.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.taskscheduler.controller.vo.TsScheduledTaskPageReqVO;
import com.spider.media.taskscheduler.entity.TsScheduledTask;
import com.spider.media.taskscheduler.mapper.TsScheduledTaskMapper;
import com.spider.media.taskscheduler.service.ITsScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 定时任务Service实现
 */
@Service
public class TsScheduledTaskServiceImpl implements ITsScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(TsScheduledTaskServiceImpl.class);

    private final TsScheduledTaskMapper scheduledTaskMapper;

    public TsScheduledTaskServiceImpl(TsScheduledTaskMapper scheduledTaskMapper) {
        this.scheduledTaskMapper = scheduledTaskMapper;
    }

    @Override
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
    public void enableTask(Long taskId) {
        TsScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_NOT_FOUND);
        }
        task.setStatus(1);
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        scheduledTaskMapper.updateById(task);
        log.info("启用任务: {}", task.getTaskName());
    }

    @Override
    public void disableTask(Long taskId) {
        TsScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.TS_TASK_NOT_FOUND);
        }
        task.setStatus(0);
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        scheduledTaskMapper.updateById(task);
        log.info("停用任务: {}", task.getTaskName());
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
}
