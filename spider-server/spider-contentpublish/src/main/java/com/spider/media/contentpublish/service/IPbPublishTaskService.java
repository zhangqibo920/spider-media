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
     * 根据主键ID查询发布任务
     *
     * @param id 任务ID
     * @return 任务实体，不存在返回 null
     */
    PbPublishTask selectById(Long id);

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
     * <p>发布前由 Controller 同步校验任务归属（@Async 异常无法直接返回前端），
     * 校验通过后再触发异步发布流程。</p>
     *
     * @param taskId     任务ID
     * @param operatorId 当前操作用户ID（用于归属校验）
     */
    void publishNow(Long taskId, Long operatorId);

    /**
     * 设置定时发布时间
     *
     * <p>校验任务归属后更新计划发布时间。</p>
     *
     * @param taskId        任务ID
     * @param scheduledTime 计划发布时间
     * @param operatorId    当前操作用户ID（用于归属校验）
     */
    void schedulePublish(Long taskId, LocalDateTime scheduledTime, Long operatorId);

    /**
     * 校验发布任务归属
     *
     * @param taskId     任务ID
     * @param operatorId 当前操作用户ID
     * @return 通过校验的任务实体
     * @throws com.spider.media.common.exception.ServiceException 任务不存在或不属于当前用户时抛出
     */
    PbPublishTask validateOwnership(Long taskId, Long operatorId);

    /**
     * 更新发布任务
     *
     * @param task 待更新的任务实体
     * @return 更新后的任务实体
     */
    PbPublishTask updateTask(PbPublishTask task);

    /**
     * 逻辑删除发布任务
     *
     * @param taskId     任务ID
     * @param operatorId 当前操作用户ID
     */
    void deleteTask(Long taskId, Long operatorId);

    /**
     * 分页查询发布任务列表
     *
     * @param pageReqVO 分页查询参数（用户ID、平台、状态）
     * @return 任务分页结果
     */
    PageResult<PbPublishTask> selectTaskPage(PbPublishTaskPageReqVO pageReqVO);
}
