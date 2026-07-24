package com.spider.media.contentpublish.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.contentpublish.controller.vo.PbPublishTaskPageReqVO;
import com.spider.media.contentpublish.entity.PbPublishTask;
import com.spider.media.contentpublish.mapper.PbPublishTaskMapper;
import com.spider.media.contentpublish.service.IPbPublishTaskService;
import com.spider.media.framework.security.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 发布任务Service实现
 */
@Service
public class PbPublishTaskServiceImpl implements IPbPublishTaskService {

    private static final Logger log = LoggerFactory.getLogger(PbPublishTaskServiceImpl.class);

    private final PbPublishTaskMapper publishTaskMapper;

    public PbPublishTaskServiceImpl(PbPublishTaskMapper publishTaskMapper) {
        this.publishTaskMapper = publishTaskMapper;
    }

    @Override
    public PbPublishTask createTask(PbPublishTask task) {
        task.setStatus(0);
        task.setCreateBy(String.valueOf(task.getUserId()));
        task.setCreateTime(LocalDateTime.now());
        publishTaskMapper.insert(task);
        return task;
    }

    @Override
    public void publishNow(Long taskId) {
        PbPublishTask task = publishTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.PB_TASK_NOT_FOUND);
        }
        task.setStatus(1);
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);

        // TODO: 实际的发布逻辑
        log.info("开始发布任务, taskId={}, platform={}", taskId, task.getPlatform());
    }

    @Override
    public void schedulePublish(Long taskId, LocalDateTime scheduledTime) {
        PbPublishTask task = publishTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.PB_TASK_NOT_FOUND);
        }
        task.setStatus(3);
        task.setScheduledTime(scheduledTime);
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);
    }

    @Override
    public PageResult<PbPublishTask> selectTaskPage(PbPublishTaskPageReqVO pageReqVO) {
        return PageUtils.selectPage(pageReqVO, () ->
                publishTaskMapper.selectPage(
                        pageReqVO.getUserId(),
                        pageReqVO.getPlatform(),
                        pageReqVO.getStatus()
                )
        );
    }
}
