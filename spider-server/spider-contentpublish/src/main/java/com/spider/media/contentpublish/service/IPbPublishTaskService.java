package com.spider.media.contentpublish.service;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.contentpublish.controller.vo.PbPublishTaskPageReqVO;
import com.spider.media.contentpublish.entity.PbPublishTask;

import java.time.LocalDateTime;

/**
 * 发布任务业务层接口
 *
 * <p>定义发布任务的创建、立即发布、定时发布、分页查询等操作。
 * 由 {@link com.spider.media.contentpublish.service.impl.PbPublishTaskServiceImpl} 提供具体实现。</p>
 */
public interface IPbPublishTaskService {

    /**
     * 创建发布任务（初始状态为草稿）
     *
     * @param task 任务实体
     * @return 创建后的任务实体
     */
    PbPublishTask createTask(PbPublishTask task);

    /**
     * 立即发布（异步执行，调用平台 API 发布内容）
     *
     * @param taskId 任务ID
     */
    void publishNow(Long taskId);

    /**
     * 设置定时发布时间
     *
     * @param taskId        任务ID
     * @param scheduledTime 计划发布时间
     */
    void schedulePublish(Long taskId, LocalDateTime scheduledTime);

    /**
     * 分页查询发布任务列表
     *
     * @param pageReqVO 分页查询参数（用户ID、平台、状态）
     * @return 任务分页结果
     */
    PageResult<PbPublishTask> selectTaskPage(PbPublishTaskPageReqVO pageReqVO);
}
