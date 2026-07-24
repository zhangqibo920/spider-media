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
import com.spider.media.contentpublish.service.IPbPublishTaskService;
import com.spider.media.framework.security.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PbPublishTaskServiceImpl implements IPbPublishTaskService {

    private static final Logger log = LoggerFactory.getLogger(PbPublishTaskServiceImpl.class);

    private final PbPublishTaskMapper publishTaskMapper;
    private final PbPlatformAccountMapper platformAccountMapper;

    public PbPublishTaskServiceImpl(PbPublishTaskMapper publishTaskMapper,
                                     PbPlatformAccountMapper platformAccountMapper) {
        this.publishTaskMapper = publishTaskMapper;
        this.platformAccountMapper = platformAccountMapper;
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
    @Async
    public void publishNow(Long taskId) {
        PbPublishTask task = publishTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.PB_TASK_NOT_FOUND);
        }
        task.setStatus(1);
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);

        log.info("开始发布任务, taskId={}, platform={}", taskId, task.getPlatform());

        try {
            PbPlatformAccount account = platformAccountMapper.selectById(task.getPlatformAccountId());
            if (account == null) {
                task.setStatus(3);
                task.setPublishResult("发布失败: 找不到平台账号");
                publishTaskMapper.updateById(task);
                return;
            }

            boolean success = publishToPlatform(task, account);

            if (success) {
                task.setStatus(2);
                task.setPublishedTime(LocalDateTime.now());
                task.setPublishResult("发布成功");
                log.info("发布成功, taskId={}, platform={}", taskId, task.getPlatform());
            } else {
                task.setStatus(3);
                task.setPublishResult("发布失败: 平台接口返回错误");
                log.warn("发布失败, taskId={}, platform={}", taskId, task.getPlatform());
            }
        } catch (Exception e) {
            task.setStatus(3);
            task.setPublishResult("发布失败: " + e.getMessage());
            log.error("发布异常, taskId={}", taskId, e);
        }

        task.setUpdateBy("system");
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);
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

    private boolean publishToPlatform(PbPublishTask task, PbPlatformAccount account) {
        String platform = task.getPlatform();
        log.info("模拟发布到{}平台, 账号: {}", platform, account.getAccountName());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true;
    }
}
