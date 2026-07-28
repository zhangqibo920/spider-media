package com.spider.media.hotmonitor.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.hotmonitor.entity.HmKeyword;
import com.spider.media.hotmonitor.service.IHmKeywordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotmonitor/keyword")
public class HmKeywordController extends BaseController {

    private final IHmKeywordService keywordService;

    public HmKeywordController(IHmKeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping("/list")
    public R<List<HmKeyword>> list() {
        Long userId = LoginUser.getUserId();
        return list(keywordService.selectList(userId));
    }

    @GetMapping("/{id}")
    public R<HmKeyword> get(@PathVariable Long id) {
        return ok(keywordService.selectById(id));
    }

    @PostMapping("/create")
    public R<HmKeyword> create(@RequestBody HmKeyword keyword) {
        Long userId = LoginUser.getUserId();
        String username = LoginUser.getUsername();
        return ok(keywordService.create(keyword, userId, username));
    }

    @PutMapping("/update")
    public R<HmKeyword> update(@RequestBody HmKeyword keyword) {
        String username = LoginUser.getUsername();
        return ok(keywordService.update(keyword, username));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        keywordService.delete(id);
        return ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestParam String status) {
        String username = LoginUser.getUsername();
        keywordService.toggleStatus(id, status, username);
        return ok();
    }
}
