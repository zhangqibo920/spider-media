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
 * 内容发布控制器
 *
 * <p>提供发布账号管理和发布任务管理的 RESTful 接口。
 * 所有接口路径在 /api/publish 下，需要用户登录后访问。</p>
 */
@RestController
@RequestMapping("/api/publish")
public class PbPublishController extends BaseController {

    /** 发布账号业务层服务 */
    private final IPbPlatformAccountService platformAccountService;
    /** 发布任务业务层服务 */
    private final IPbPublishTaskService publishTaskService;

    public PbPublishController(IPbPlatformAccountService platformAccountService,
                                IPbPublishTaskService publishTaskService) {
        this.platformAccountService = platformAccountService;
        this.publishTaskService = publishTaskService;
    }

    // ========== 发布账号管理 ==========

    /**
     * 查询当前用户的发布账号列表
     *
     * @return 发布账号列表
     */
    @GetMapping("/account/list")
    public R<List<PbPlatformAccount>> listAccounts() {
        Long userId = LoginUser.getUserId();
        return list(platformAccountService.selectAccountList(userId));
    }

    /**
     * 添加发布账号
     *
     * @param account 待新增的发布账号实体
     * @return 操作结果
     */
    @PostMapping("/account")
    public R<Integer> addAccount(@RequestBody PbPlatformAccount account) {
        account.setUserId(LoginUser.getUserId());
        return ok(platformAccountService.insertAccount(account));
    }

    /**
     * 删除发布账号
     *
     * @param id 发布账号主键ID
     * @return 操作结果
     */
    @DeleteMapping("/account/{id}")
    public R<Integer> deleteAccount(@PathVariable Long id) {
        return ok(platformAccountService.deleteAccountById(id));
    }

    // ========== 发布任务管理 ==========

    /**
     * 创建发布任务（初始状态为草稿）
     *
     * @param task 发布任务实体
     * @return 创建后的任务实体
     */
    @PostMapping("/task")
    public R<PbPublishTask> createTask(@RequestBody PbPublishTask task) {
        task.setUserId(LoginUser.getUserId());
        return ok(publishTaskService.createTask(task));
    }

    /**
     * 立即发布（异步执行）
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @PostMapping("/task/{id}/publish")
    public R<Void> publishNow(@PathVariable Long id) {
        publishTaskService.publishNow(id);
        return ok();
    }

    /**
     * 设置定时发布时间
     *
     * @param id      任务ID
     * @param request 包含 scheduledTime 的 JSON 请求体
     * @return 操作结果
     */
    @PostMapping("/task/{id}/schedule")
    public R<Void> schedulePublish(@PathVariable Long id, @RequestBody Map<String, String> request) {
        LocalDateTime scheduledTime = LocalDateTime.parse(request.get("scheduledTime"));
        publishTaskService.schedulePublish(id, scheduledTime);
        return ok();
    }

    /**
     * 分页查询发布任务列表
     *
     * @param pageReqVO 分页查询参数
     * @return 任务分页结果
     */
    @GetMapping("/task/page")
    public R<PageResult<PbPublishTask>> taskPage(PbPublishTaskPageReqVO pageReqVO) {
        return page(publishTaskService.selectTaskPage(pageReqVO));
    }
}
