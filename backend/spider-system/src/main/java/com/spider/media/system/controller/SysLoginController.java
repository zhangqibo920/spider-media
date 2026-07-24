package com.spider.media.system.controller;

import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.service.ISysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class SysLoginController {

    private final ISysUserService userService;

    public SysLoginController(ISysUserService userService) {
        this.userService = userService;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> loginBody) {
        String token = userService.login(loginBody.get("username"), loginBody.get("password"));
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return R.ok(result);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R<SysUser> register(@RequestBody Map<String, String> registerBody) {
        SysUser user = userService.register(
                registerBody.get("username"),
                registerBody.get("password"),
                registerBody.get("email")
        );
        return R.ok(user);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/getInfo")
    public R<SysUser> getInfo() {
        String username = LoginUser.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        return R.ok(user);
    }
}
