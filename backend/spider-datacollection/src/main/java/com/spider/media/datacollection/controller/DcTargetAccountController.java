package com.spider.media.datacollection.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.datacollection.controller.vo.DcCollectedArticlePageReqVO;
import com.spider.media.datacollection.entity.DcCollectedArticle;
import com.spider.media.datacollection.entity.DcTargetAccount;
import com.spider.media.datacollection.service.IDcCollectedArticleService;
import com.spider.media.datacollection.service.IDcTargetAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据采集控制器
 *
 * <p>提供对标账号管理和文章采集的 RESTful 接口。
 * 所有接口路径在 /api/collection 下，需要用户登录后访问。</p>
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
     * @param account 包含筛选条件（平台、分组）的对标账号实体
     * @return 对标账号列表
     */
    @GetMapping("/account/list")
    public R<List<DcTargetAccount>> list(DcTargetAccount account) {
        return list(targetAccountService.selectTargetAccountList(account));
    }

    /**
     * 新增对标账号
     *
     * @param account 待新增的对标账号实体
     * @return 操作结果
     */
    @PostMapping("/account")
    public R<Integer> add(@RequestBody DcTargetAccount account) {
        return ok(targetAccountService.insertTargetAccount(account));
    }

    /**
     * 删除对标账号
     *
     * @param id 对标账号主键ID
     * @return 操作结果
     */
    @DeleteMapping("/account/{id}")
    public R<Integer> remove(@PathVariable Long id) {
        return ok(targetAccountService.deleteTargetAccountById(id));
    }

    /**
     * 触发对标账号的文章采集任务（异步执行）
     *
     * @param id 对标账号ID
     * @return 操作结果
     */
    @PostMapping("/account/{id}/collect")
    public R<Void> collect(@PathVariable Long id) {
        collectedArticleService.collectArticles(id);
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
