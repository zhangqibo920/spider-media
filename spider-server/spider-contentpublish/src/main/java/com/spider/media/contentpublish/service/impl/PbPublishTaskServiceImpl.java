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

/**
 * 发布任务业务层实现类
 *
 * <p>实现发布任务的完整生命周期管理：
 * <ul>
 *   <li>创建任务（草稿状态）</li>
 *   <li>立即发布（异步调用平台 API）</li>
 *   <li>设置定时发布</li>
 *   <li>分页查询</li>
 * </ul></p>
 *
 * <p>发布流程：查找平台账号 → 验证账号有效性 → 调用平台 API → 更新任务状态和结果。</p>
 */
@Service
public class PbPublishTaskServiceImpl implements IPbPublishTaskService {

    private static final Logger log = LoggerFactory.getLogger(PbPublishTaskServiceImpl.class);

    /** 发布任务数据访问对象 */
    private final PbPublishTaskMapper publishTaskMapper;
    /** 平台账号数据访问对象（用于查找发布所需的授权账号） */
    private final PbPlatformAccountMapper platformAccountMapper;

    public PbPublishTaskServiceImpl(PbPublishTaskMapper publishTaskMapper,
                                     PbPlatformAccountMapper platformAccountMapper) {
        this.publishTaskMapper = publishTaskMapper;
        this.platformAccountMapper = platformAccountMapper;
    }

    /**
     * 创建发布任务（初始状态为草稿）
     */
    @Override
    public PbPublishTask createTask(PbPublishTask task) {
        task.setStatus(0);
        task.setCreateBy(String.valueOf(task.getUserId()));
        task.setCreateTime(LocalDateTime.now());
        publishTaskMapper.insert(task);
        return task;
    }

    /**
     * 异步立即发布任务
     *
     * <p>发布流程：
     * <ol>
     *   <li>查询发布任务，校验是否存在</li>
     *   <li>将状态更新为"发布中"</li>
     *   <li>查找关联的平台账号</li>
     *   <li>调用平台 API 发布内容</li>
     *   <li>根据结果更新状态为"已发布"或"失败"</li>
     * </ol></p>
     *
     * @param taskId 任务ID
     */
    @Override
    @Async
    public void publishNow(Long taskId) {
        PbPublishTask task = publishTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException(ErrorCodeEnums.PB_TASK_NOT_FOUND);
        }
        // 更新状态为发布中
        task.setStatus(1);
        task.setUpdateBy(LoginUser.getUsername());
        task.setUpdateTime(LocalDateTime.now());
        publishTaskMapper.updateById(task);

        log.info("开始发布任务, taskId={}, platform={}", taskId, task.getPlatform());

        try {
            // 查找平台账号
            PbPlatformAccount account = platformAccountMapper.selectById(task.getPlatformAccountId());
            if (account == null) {
                task.setStatus(3);
                task.setPublishResult("发布失败: 找不到平台账号");
                publishTaskMapper.updateById(task);
                return;
            }

            // 调用平台 API 发布
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

    /**
     * 设置定时发布时间
     *
     * @throws ServiceException 任务不存在时抛出异常
     */
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

    /**
     * 模拟发布到平台（实际项目中替换为真实的平台 API 调用）
     *
     * @param task    发布任务
     * @param account 发布使用的平台账号
     * @return 发布是否成功
     */
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
