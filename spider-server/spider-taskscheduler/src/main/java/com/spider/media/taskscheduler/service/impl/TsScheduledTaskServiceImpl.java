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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 定时任务业务层实现类
 *
 * <p>实现定时任务的创建、启用、停用、分页查询等操作。
 * 创建时初始化执行次数和失败次数为 0，状态默认为停止。
 * 启用/停用时会校验任务是否存在。</p>
 */
@Service
public class TsScheduledTaskServiceImpl implements ITsScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(TsScheduledTaskServiceImpl.class);

    /** 定时任务数据访问对象 */
    private final TsScheduledTaskMapper scheduledTaskMapper;

    public TsScheduledTaskServiceImpl(TsScheduledTaskMapper scheduledTaskMapper) {
        this.scheduledTaskMapper = scheduledTaskMapper;
    }

    /**
     * 创建定时任务
     *
     * <p>初始化任务状态为停止（0），执行次数和失败次数为 0。</p>
     */
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

    /**
     * 启用定时任务
     *
     * @throws ServiceException 任务不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 停用定时任务
     *
     * @throws ServiceException 任务不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
