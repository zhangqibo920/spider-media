package com.spider.media.taskscheduler.service;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.taskscheduler.controller.vo.TsScheduledTaskPageReqVO;
import com.spider.media.taskscheduler.entity.TsScheduledTask;

/**
 * 定时任务Service接口
 */
public interface ITsScheduledTaskService {

    TsScheduledTask createTask(TsScheduledTask task);

    void enableTask(Long taskId);

    void disableTask(Long taskId);

    /**
     * 查询定时任务分页列表
     */
    PageResult<TsScheduledTask> selectTaskPage(TsScheduledTaskPageReqVO pageReqVO);
}
