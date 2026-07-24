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
 * 数据采集Controller
 */
@RestController
@RequestMapping("/api/collection")
public class DcTargetAccountController extends BaseController {

    private final IDcTargetAccountService targetAccountService;
    private final IDcCollectedArticleService collectedArticleService;

    public DcTargetAccountController(IDcTargetAccountService targetAccountService,
                                      IDcCollectedArticleService collectedArticleService) {
        this.targetAccountService = targetAccountService;
        this.collectedArticleService = collectedArticleService;
    }

    /**
     * 查询对标账号列表
     */
    @GetMapping("/account/list")
    public R<List<DcTargetAccount>> list(DcTargetAccount account) {
        return list(targetAccountService.selectTargetAccountList(account));
    }

    /**
     * 新增对标账号
     */
    @PostMapping("/account")
    public R<Integer> add(@RequestBody DcTargetAccount account) {
        return ok(targetAccountService.insertTargetAccount(account));
    }

    /**
     * 删除对标账号
     */
    @DeleteMapping("/account/{id}")
    public R<Integer> remove(@PathVariable Long id) {
        return ok(targetAccountService.deleteTargetAccountById(id));
    }

    /**
     * 触发采集任务
     */
    @PostMapping("/account/{id}/collect")
    public R<Void> collect(@PathVariable Long id) {
        collectedArticleService.collectArticles(id);
        return ok();
    }

    /**
     * 查询采集文章分页列表
     */
    @GetMapping("/article/page")
    public R<PageResult<DcCollectedArticle>> articlePage(DcCollectedArticlePageReqVO pageReqVO) {
        return page(collectedArticleService.selectArticlePage(pageReqVO));
    }
}
