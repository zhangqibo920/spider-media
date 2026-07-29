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
import jakarta.validation.Valid;
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
    public R<Integer> addAccount(@Valid @RequestBody PbPlatformAccount account) {
        account.setUserId(LoginUser.getUserId());
        return ok(platformAccountService.insertAccount(account));
    }

    /**
     * 更新发布账号
     *
     * @param account 待更新的账号实体
     * @return 操作结果
     */
    @PutMapping("/account")
    public R<Integer> updateAccount(@Valid @RequestBody PbPlatformAccount account) {
        account.setUserId(LoginUser.getUserId());
        return ok(platformAccountService.updateAccount(account));
    }

    /**
     * 删除发布账号
     *
     * <p>删除前校验账号归属，仅允许删除自己的平台账号。</p>
     *
     * @param id 发布账号主键ID
     * @return 操作结果
     */
    @DeleteMapping("/account/{id}")
    public R<Integer> deleteAccount(@PathVariable Long id) {
        return ok(platformAccountService.deleteAccountById(id, LoginUser.getUserId()));
    }

    // ========== 发布任务管理 ==========

    /**
     * 创建发布任务（初始状态为草稿）
     *
     * @param task 发布任务实体
     * @return 创建后的任务实体
     */
    @PostMapping("/task")
    public R<PbPublishTask> createTask(@Valid @RequestBody PbPublishTask task) {
        task.setUserId(LoginUser.getUserId());
        return ok(publishTaskService.createTask(task));
    }

    /**
     * 更新发布任务
     *
     * @param task 待更新的任务实体
     * @return 更新后的任务实体
     */
    @PutMapping("/task")
    public R<PbPublishTask> updateTask(@Valid @RequestBody PbPublishTask task) {
        task.setUserId(LoginUser.getUserId());
        return ok(publishTaskService.updateTask(task));
    }

    /**
     * 删除发布任务
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/task/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        publishTaskService.deleteTask(id, LoginUser.getUserId());
        return ok();
    }

    /**
     * 立即发布（异步执行）
     *
     * <p>发布前先同步校验任务归属（@Async 方法异常无法直接返回前端），
     * 校验通过后再触发异步发布流程。</p>
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @PostMapping("/task/{id}/publish")
    public R<Void> publishNow(@PathVariable Long id) {
        Long userId = LoginUser.getUserId();
        // 同步校验归属，失败抛 ServiceException 直接返回前端
        publishTaskService.validateOwnership(id, userId);
        // 校验通过后触发异步发布
        publishTaskService.publishNow(id, userId);
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
        String scheduledTimeStr = request.get("scheduledTime");
        if (scheduledTimeStr == null || scheduledTimeStr.isBlank()) {
            throw new com.spider.media.common.exception.ServiceException(
                    com.spider.media.common.result.ErrorCodeEnums.PARAM_ERROR, "scheduledTime 不能为空");
        }
        LocalDateTime scheduledTime;
        try {
            scheduledTime = LocalDateTime.parse(scheduledTimeStr);
        } catch (Exception e) {
            throw new com.spider.media.common.exception.ServiceException(
                    com.spider.media.common.result.ErrorCodeEnums.PARAM_ERROR, "scheduledTime 格式不正确");
        }
        publishTaskService.schedulePublish(id, scheduledTime, LoginUser.getUserId());
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
        pageReqVO.setUserId(LoginUser.getUserId());
        return page(publishTaskService.selectTaskPage(pageReqVO));
    }
}
