package com.spider.media.contentpublish.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.contentpublish.controller.vo.PbPublishTaskPageReqVO;
import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.entity.PbPublishTask;
import com.spider.media.contentpublish.service.IPbPlatformAccountService;
import com.spider.media.contentpublish.service.IPbPublishTaskService;
import com.spider.media.framework.security.LoginUser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 内容发布Controller
 */
@RestController
@RequestMapping("/api/publish")
public class PbPublishController extends BaseController {

    private final IPbPlatformAccountService platformAccountService;
    private final IPbPublishTaskService publishTaskService;

    public PbPublishController(IPbPlatformAccountService platformAccountService,
                                IPbPublishTaskService publishTaskService) {
        this.platformAccountService = platformAccountService;
        this.publishTaskService = publishTaskService;
    }

    /**
     * 查询发布账号列表
     */
    @GetMapping("/account/list")
    public R<List<PbPlatformAccount>> listAccounts() {
        Long userId = LoginUser.getUserId();
        return list(platformAccountService.selectAccountList(userId));
    }

    /**
     * 添加发布账号
     */
    @PostMapping("/account")
    public R<Integer> addAccount(@RequestBody PbPlatformAccount account) {
        account.setUserId(LoginUser.getUserId());
        return ok(platformAccountService.insertAccount(account));
    }

    /**
     * 删除发布账号
     */
    @DeleteMapping("/account/{id}")
    public R<Integer> deleteAccount(@PathVariable Long id) {
        return ok(platformAccountService.deleteAccountById(id));
    }

    /**
     * 创建发布任务
     */
    @PostMapping("/task")
    public R<PbPublishTask> createTask(@RequestBody PbPublishTask task) {
        task.setUserId(LoginUser.getUserId());
        return ok(publishTaskService.createTask(task));
    }

    /**
     * 立即发布
     */
    @PostMapping("/task/{id}/publish")
    public R<Void> publishNow(@PathVariable Long id) {
        publishTaskService.publishNow(id);
        return ok();
    }

    /**
     * 定时发布
     */
    @PostMapping("/task/{id}/schedule")
    public R<Void> schedulePublish(@PathVariable Long id, @RequestBody Map<String, String> request) {
        LocalDateTime scheduledTime = LocalDateTime.parse(request.get("scheduledTime"));
        publishTaskService.schedulePublish(id, scheduledTime);
        return ok();
    }

    /**
     * 查询发布任务分页列表
     */
    @GetMapping("/task/page")
    public R<PageResult<PbPublishTask>> taskPage(PbPublishTaskPageReqVO pageReqVO) {
        return page(publishTaskService.selectTaskPage(pageReqVO));
    }
}
