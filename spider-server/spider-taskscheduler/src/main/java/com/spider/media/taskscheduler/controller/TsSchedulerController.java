package com.spider.media.taskscheduler.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.taskscheduler.controller.vo.TsScheduledTaskPageReqVO;
import com.spider.media.taskscheduler.entity.TsScheduledTask;
import com.spider.media.taskscheduler.service.ITsScheduledTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 任务调度控制器
 *
 * <p>提供定时任务的创建、启用、停用、分页查询等 RESTful 接口。
 * 所有接口路径在 /api/scheduler 下，需要用户登录后访问。</p>
 */
@RestController
@RequestMapping("/api/scheduler")
public class TsSchedulerController extends BaseController {

    /** 定时任务业务层服务 */
    private final ITsScheduledTaskService scheduledTaskService;

    public TsSchedulerController(ITsScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

    /**
     * 创建定时任务
     *
     * @param task 任务实体（包含任务名、类型、Cron表达式等）
     * @return 创建后的任务实体
     */
    @PostMapping("/task")
    public R<TsScheduledTask> createTask(@Valid @RequestBody TsScheduledTask task) {
        task.setUserId(LoginUser.getUserId());
        return ok(scheduledTaskService.createTask(task));
    }

    /**
     * 启用定时任务
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @PostMapping("/task/{id}/enable")
    public R<Void> enableTask(@PathVariable Long id) {
        scheduledTaskService.enableTask(id);
        return ok();
    }

    /**
     * 停用定时任务
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @PostMapping("/task/{id}/disable")
    public R<Void> disableTask(@PathVariable Long id) {
        scheduledTaskService.disableTask(id);
        return ok();
    }

    /**
     * 分页查询定时任务列表
     *
     * @param pageReqVO 分页查询参数
     * @return 任务分页结果
     */
    @GetMapping("/task/page")
    public R<PageResult<TsScheduledTask>> taskPage(@Valid TsScheduledTaskPageReqVO pageReqVO) {
        pageReqVO.setUserId(LoginUser.getUserId());
        return page(scheduledTaskService.selectTaskPage(pageReqVO));
    }
}
