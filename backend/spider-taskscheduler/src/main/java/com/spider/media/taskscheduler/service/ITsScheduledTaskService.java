package com.spider.media.taskscheduler.service;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.taskscheduler.controller.vo.TsScheduledTaskPageReqVO;
import com.spider.media.taskscheduler.entity.TsScheduledTask;

/**
 * 定时任务业务层接口
 *
 * <p>定义定时任务的创建、启用、停用、分页查询等操作。
 * 由 {@link com.spider.media.taskscheduler.service.impl.TsScheduledTaskServiceImpl} 提供具体实现。</p>
 */
public interface ITsScheduledTaskService {

    /**
     * 创建定时任务
     *
     * @param task 任务实体（包含任务名、类型、Cron表达式等）
     * @return 创建后的任务实体（包含ID和初始状态）
     */
    TsScheduledTask createTask(TsScheduledTask task);

    /**
     * 启用定时任务（将状态从停止改为运行中）
     *
     * @param taskId 任务ID
     */
    void enableTask(Long taskId);

    /**
     * 停用定时任务（将状态从运行中改为停止）
     *
     * @param taskId 任务ID
     */
    void disableTask(Long taskId);

    /**
     * 分页查询定时任务列表
     *
     * @param pageReqVO 分页查询参数（用户ID、任务名、状态）
     * @return 任务分页结果
     */
    PageResult<TsScheduledTask> selectTaskPage(TsScheduledTaskPageReqVO pageReqVO);
}
