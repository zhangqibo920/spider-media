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
 *
 * <p>处理用户登录、注册、获取当前用户信息等认证相关的接口。
 * 所有接口路径在 /api/auth 下，Spring Security 配置中已设置为无需认证即可访问。</p>
 */
@RestController
@RequestMapping("/api/auth")
public class SysLoginController {

    /** 用户业务层服务 */
    private final ISysUserService userService;

    public SysLoginController(ISysUserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     *
     * <p>接收用户名和密码，验证通过后返回 JWT Token。
     * 前端需将 Token 存储在本地，后续请求在 Authorization 头中携带。</p>
     *
     * @param loginBody 包含 username 和 password 的 JSON 请求体
     * @return 包含 token 的响应数据
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> loginBody) {
        String token = userService.login(loginBody.get("username"), loginBody.get("password"));
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return R.ok(result);
    }

    /**
     * 用户注册
     *
     * <p>接收用户名和密码，创建新用户账号。注册成功后返回用户信息（不含密码）。</p>
     *
     * @param registerBody 包含 username 和 password 的 JSON 请求体
     * @return 注册成功后的用户信息
     */
    @PostMapping("/register")
    public R<SysUser> register(@RequestBody Map<String, String> registerBody) {
        SysUser user = userService.register(
                registerBody.get("username"),
                registerBody.get("password")
        );
        return R.ok(user);
    }

    /**
     * 获取当前登录用户信息
     *
     * <p>从 SecurityContext 中提取当前登录用户的用户名，查询并返回完整的用户信息。
     * 需要在请求头中携带有效的 JWT Token。</p>
     *
     * @return 当前登录用户的详细信息
     */
    @GetMapping("/getInfo")
    public R<SysUser> getInfo() {
        String username = LoginUser.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        return R.ok(user);
    }
}
