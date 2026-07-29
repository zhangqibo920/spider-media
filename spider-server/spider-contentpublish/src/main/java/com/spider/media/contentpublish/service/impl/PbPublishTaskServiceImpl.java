package com.spider.media.contentpublish.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.contentpublish.controller.vo.PbPublishTaskPageReqVO;
import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.entity.PbPublishTask;
import com.spider.media.contentpublish.mapper.PbPlatformAccountMapper;
import com.spider.media.contentpublish.mapper.PbPublishTaskMapper;
import com.spider.media.contentpublish.publisher.PlatformPublisher;
import com.spider.media.contentpublish.publisher.PlatformPublisherRegistry;
import com.spider.media.contentpublish.service.IPbPublishTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PbPublishTaskServiceImpl implements IPbPublishTaskService {

    private static final Logger log = LoggerFactory.getLogger(PbPublishTaskServiceImpl.class);
    private static final int MAX_RETRIES = 3;

    private final PbPublishTaskMapper publishTaskMapper;
    private final PbPlatformAccountMapper platformAccountMapper;
    private final PlatformPublisherRegistry publisherRegistry;

    public PbPublishTaskServiceImpl(PbPublishTaskMapper publishTaskMapper,
                                     PbPlatformAccountMapper platformAccountMapper,
                                     PlatformPublisherRegistry publisherRegistry) {
        this.publishTaskMapper = publishTaskMapper;
        this.platformAccountMapper = platformAccountMapper;
        this.publisherRegistry = publisherRegistry;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PbPublishTask createTask(PbPublishTask task) {
        task.setStatus(0);
        task.setCreateBy(String.valueOf(task.getUserId()));
        task.setCreateTime(LocalDateTime.now());
        publishTaskMapper.insert(task);
        return task;
    }

    @Override
    @Async
    public void publishNow(Long taskId, Long operatorId) {
        PbPublishTask task = publishTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("发布任务不存在, taskId={}", taskId);
            return;
        }
        if (operatorId == null || !operatorId.equals(task.getUserId())) {
            log.warn("越权发布被拒绝, taskId={}, operatorId={}, ownerId={}",
                    taskId, operatorId, task.getUserId());
            return;
        }

        task.setStatus(1);
        task.setUpdateBy(String.valueOf(operatorId));
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);

        log.info("开始发布任务, taskId={}, platform={}", taskId, task.getPlatform());

        try {
            PbPlatformAccount account = platformAccountMapper.selectById(task.getPlatformAccountId());
            if (account == null) {
                markFailed(task, "找不到平台账号");
                return;
            }

            PlatformPublisher publisher = publisherRegistry.getPublisher(task.getPlatform());
            PlatformPublisher.PublishResult result = publisher.publish(task, account);

            if (result.success()) {
                task.setStatus(2);
                task.setPublishedTime(LocalDateTime.now());
                task.setPublishResult(result.message());
                log.info("发布成功, taskId={}", taskId);
            } else {
                handleFailure(task, result.message());
            }
        } catch (Exception e) {
            handleFailure(task, "发布异常: " + e.getMessage());
        }

        task.setUpdateBy("system");
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedulePublish(Long taskId, LocalDateTime scheduledTime, Long operatorId) {
        PbPublishTask task = validateOwnership(taskId, operatorId);
        task.setScheduledTime(scheduledTime);
        task.setUpdateBy(String.valueOf(operatorId));
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PbPublishTask updateTask(PbPublishTask task) {
        validateOwnership(task.getId(), task.getUserId());
        task.setUpdateBy(String.valueOf(task.getUserId()));
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long operatorId) {
        validateOwnership(taskId, operatorId);
        publishTaskMapper.deleteById(taskId);
    }

    @Override
    public PbPublishTask validateOwnership(Long taskId, Long operatorId) {
        PbPublishTask task = publishTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.PB_TASK_NOT_FOUND);
        }
        if (operatorId == null || !operatorId.equals(task.getUserId())) {
            throw new ServiceException(ErrorCodeEnums.FORBIDDEN, "无权操作他人的发布任务");
        }
        return task;
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

    private void handleFailure(PbPublishTask task, String reason) {
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        if (retryCount < MAX_RETRIES) {
            task.setStatus(1);
            task.setRetryCount(retryCount + 1);
            task.setPublishResult("第" + (retryCount + 1) + "次重试: " + reason);
            log.warn("发布失败即将重试, taskId={}, retry={}/{}", task.getId(), retryCount + 1, MAX_RETRIES);
        } else {
            task.setStatus(3);
            task.setPublishResult(reason);
            log.warn("发布失败已达最大重试次数, taskId={}", task.getId());
        }
    }

    private void markFailed(PbPublishTask task, String reason) {
        task.setStatus(3);
        task.setPublishResult(reason);
    }
}
