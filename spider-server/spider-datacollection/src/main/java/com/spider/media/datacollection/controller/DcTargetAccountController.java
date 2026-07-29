package com.spider.media.datacollection.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.datacollection.controller.vo.DcCollectedArticlePageReqVO;
import com.spider.media.datacollection.entity.DcCollectedArticle;
import com.spider.media.datacollection.entity.DcTargetAccount;
import com.spider.media.datacollection.service.IDcCollectedArticleService;
import com.spider.media.datacollection.service.IDcTargetAccountService;
import com.spider.media.framework.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据采集控制器
 *
 * <p>提供对标账号管理和文章采集的 RESTful 接口。
 * 所有接口路径在 /api/collection 下，需要用户登录后访问。</p>
 *
 * <p>所有写操作（新增/删除/采集）都强制绑定当前登录用户ID，
 * 查询、删除、采集操作均校验数据归属，防止横向越权。</p>
 */
@RestController
@RequestMapping("/api/collection")
public class DcTargetAccountController extends BaseController {

    /** 对标账号业务层服务 */
    private final IDcTargetAccountService targetAccountService;
    /** 采集文章业务层服务 */
    private final IDcCollectedArticleService collectedArticleService;

    public DcTargetAccountController(IDcTargetAccountService targetAccountService,
                                      IDcCollectedArticleService collectedArticleService) {
        this.targetAccountService = targetAccountService;
        this.collectedArticleService = collectedArticleService;
    }

    /**
     * 查询对标账号列表
     *
     * <p>Service 层会自动注入当前登录用户ID作为过滤条件，确保只能看到自己的对标账号。</p>
     *
     * @param account 包含筛选条件（平台、分组）的对标账号实体
     * @return 对标账号列表
     */
    @GetMapping("/account/list")
    public R<List<DcTargetAccount>> list(DcTargetAccount account) {
        return list(targetAccountService.selectTargetAccountList(account));
    }

    @GetMapping("/account/{id}")
    public R<DcTargetAccount> getTargetAccount(@PathVariable Long id) {
        return ok(targetAccountService.selectById(id));
    }

    /**
     * 新增对标账号
     *
     * <p>Service 层会自动绑定当前登录用户ID，前端传入的 userId 字段会被覆盖。</p>
     *
     * @param account 待新增的对标账号实体
     * @return 操作结果
     */
    @PostMapping("/account")
    public R<Integer> add(@Valid @RequestBody DcTargetAccount account) {
        return ok(targetAccountService.insertTargetAccount(account));
    }

    /**
     * 更新对标账号
     *
     * <p>更新前校验账号归属，仅允许更新自己的对标账号。</p>
     *
     * @param account 包含更新字段的对标账号实体
     * @return 操作结果
     */
    @PutMapping("/account")
    public R<Integer> edit(@Valid @RequestBody DcTargetAccount account) {
        return ok(targetAccountService.updateTargetAccount(account));
    }

    /**
     * 删除对标账号
     *
     * <p>删除前校验账号归属，仅允许删除自己的对标账号。</p>
     *
     * @param id 对标账号主键ID
     * @return 操作结果
     */
    @DeleteMapping("/account/{id}")
    public R<Integer> remove(@PathVariable Long id) {
        return ok(targetAccountService.deleteTargetAccountById(id, LoginUser.getUserId()));
    }

    /**
     * 删除单篇采集文章
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @DeleteMapping("/article/{id}")
    public R<Void> removeArticle(@PathVariable Long id) {
        collectedArticleService.deleteArticleById(id, LoginUser.getUserId());
        return ok();
    }

    /**
     * 触发对标账号的文章采集任务（异步执行）
     *
     * <p>采集前先同步校验账号归属（@Async 方法内的异常无法直接返回前端），
     * 校验通过后再触发异步采集任务。</p>
     *
     * @param id 对标账号ID
     * @return 操作结果
     */
    @PostMapping("/account/{id}/collect")
    public R<Void> collect(@PathVariable Long id) {
        Long userId = LoginUser.getUserId();
        // 同步校验归属，失败抛 ServiceException 直接返回前端
        targetAccountService.validateOwnership(id, userId);
        // 校验通过后触发异步采集
        collectedArticleService.collectArticles(id, userId);
        return ok();
    }

    /**
     * 分页查询采集文章列表
     *
     * @param pageReqVO 分页查询参数（对标账号ID、平台、标题）
     * @return 文章分页结果
     */
    @GetMapping("/article/page")
    public R<PageResult<DcCollectedArticle>> articlePage(DcCollectedArticlePageReqVO pageReqVO) {
        return page(collectedArticleService.selectArticlePage(pageReqVO));
    }
}
