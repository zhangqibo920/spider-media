package com.spider.media.taskscheduler.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.taskscheduler.controller.vo.TsScheduledTaskPageReqVO;
import com.spider.media.taskscheduler.entity.TsScheduledTask;
import com.spider.media.taskscheduler.service.ITsScheduledTaskService;
import org.springframework.web.bind.annotation.*;

/**
 * 任务调度Controller
 */
@RestController
@RequestMapping("/scheduler")
public class TsSchedulerController extends BaseController {

    private final ITsScheduledTaskService scheduledTaskService;

    public TsSchedulerController(ITsScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

    /**
     * 创建定时任务
     */
    @PostMapping("/task")
    public R<TsScheduledTask> createTask(@RequestBody TsScheduledTask task) {
        task.setUserId(LoginUser.getUserId());
        return ok(scheduledTaskService.createTask(task));
    }

    /**
     * 启用任务
     */
    @PostMapping("/task/{id}/enable")
    public R<Void> enableTask(@PathVariable Long id) {
        scheduledTaskService.enableTask(id);
        return ok();
    }

    /**
     * 停用任务
     */
    @PostMapping("/task/{id}/disable")
    public R<Void> disableTask(@PathVariable Long id) {
        scheduledTaskService.disableTask(id);
        return ok();
    }

    /**
     * 查询任务分页列表
     */
    @GetMapping("/task/page")
    public R<PageResult<TsScheduledTask>> taskPage(TsScheduledTaskPageReqVO pageReqVO) {
        return page(scheduledTaskService.selectTaskPage(pageReqVO));
    }
}
