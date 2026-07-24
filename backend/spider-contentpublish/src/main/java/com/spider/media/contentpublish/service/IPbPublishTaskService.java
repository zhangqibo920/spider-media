package com.spider.media.contentpublish.service;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.contentpublish.controller.vo.PbPublishTaskPageReqVO;
import com.spider.media.contentpublish.entity.PbPublishTask;

import java.time.LocalDateTime;

/**
 * 发布任务Service接口
 */
public interface IPbPublishTaskService {

    PbPublishTask createTask(PbPublishTask task);

    void publishNow(Long taskId);

    void schedulePublish(Long taskId, LocalDateTime scheduledTime);

    /**
     * 查询发布任务分页列表
     */
    PageResult<PbPublishTask> selectTaskPage(PbPublishTaskPageReqVO pageReqVO);
}
