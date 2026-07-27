package com.spider.media.system.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysMenu;
import com.spider.media.system.service.ISysMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class SysMenuController extends BaseController {

    private final ISysMenuService menuService;

    public SysMenuController(ISysMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/getRouters")
    public R<List<SysMenu>> getRouters() {
        Long userId = LoginUser.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return ok(menus);
    }
}
