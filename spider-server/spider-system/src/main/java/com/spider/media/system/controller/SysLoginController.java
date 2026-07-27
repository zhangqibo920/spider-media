package com.spider.media.system.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.aspect.OperLog;
import com.spider.media.system.entity.SysRole;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.service.CaptchaService;
import com.spider.media.system.service.ISysRoleService;
import com.spider.media.system.service.ISysUserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 *
 * <p>处理用户登录、注册、获取当前用户信息等认证相关的接口。
 * 所有接口路径在 /api/auth 下，Spring Security 配置中已设置为无需认证即可访问。</p>
 *
 * <p>登录流程包含图形验证码校验，防止自动化脚本暴力破解：
 * <ol>
 *   <li>前端调用 GET /api/auth/captcha 获取验证码图片和 captchaId</li>
 *   <li>前端在登录请求中携带 captchaId 和 captchaCode</li>
 *   <li>服务器校验验证码通过后再校验用户名密码</li>
 * </ol></p>
 */
@RestController
@RequestMapping("/api/auth")
public class SysLoginController extends BaseController {

    /** 用户业务层服务 */
    private final ISysUserService userService;
    /** 验证码服务 */
    private final CaptchaService captchaService;
    /** 角色业务层服务 */
    private final ISysRoleService roleService;

    public SysLoginController(ISysUserService userService, CaptchaService captchaService,
                               ISysRoleService roleService) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.roleService = roleService;
    }

    /**
     * 生成图形验证码
     *
     * <p>返回 Base64 编码的 PNG 图片和验证码唯一标识 captchaId。
     * 验证码有效期 5 分钟，且只能使用一次。</p>
     *
     * @return Map 包含 captchaId 和 img（Base64 图片）
     */
    @GetMapping("/captcha")
    public R<Map<String, String>> captcha() {
        return ok(captchaService.generateCaptcha());
    }

    /**
     * 用户登录
     *
     * <p>接收用户名、密码和验证码，校验顺序：
     * <ol>
     *   <li>校验图形验证码（captchaId + captchaCode）</li>
     *   <li>校验用户名密码</li>
     * </ol></p>
     *
     * <p>验证码校验失败会直接返回，不会触发账号失败计数。
     * 密码错误会累加失败次数，达到阈值后临时锁定账号。</p>
     *
     * @param loginBody 包含 username、password、captchaId、captchaCode 的 JSON 请求体
     * @return 包含 token 的响应数据
     */
    @OperLog(module = "认证管理", action = "登录")
    @PostMapping("/login")
    @RateLimiter(name = "login-endpoint", fallbackMethod = "loginRateLimitFallback")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> loginBody) {
        // 1. 校验验证码
        String captchaId = loginBody.get("captchaId");
        String captchaCode = loginBody.get("captchaCode");
        if (!captchaService.validateCaptcha(captchaId, captchaCode)) {
            throw new ServiceException(ErrorCodeEnums.SYS_CAPTCHA_ERROR);
        }

        // 2. 校验用户名密码
        String token = userService.login(loginBody.get("username"), loginBody.get("password"));
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return ok(result);
    }

    /**
     * 登录接口限流降级方法
     *
     * <p>当登录请求触发 RateLimiter 限流时（如 1 秒内超过 10 次请求），
     * 返回 429 状态码提示用户稍后重试，防止接口被恶意刷。</p>
     *
     * @param loginBody 原始请求体
     * @param throwable 触发降级的异常
     * @return 限流提示响应
     */
    @SuppressWarnings("unused")
    public R<Map<String, Object>> loginRateLimitFallback(Map<String, String> loginBody, Throwable throwable) {
        return R.fail(429, "登录请求过于频繁，请稍后再试");
    }

    /**
     * 用户注册
     *
     * <p>接收用户名和密码，创建新用户账号。注册成功后返回用户信息（不含密码）。</p>
     *
     * @param registerBody 包含 username 和 password 的 JSON 请求体
     * @return 注册成功后的用户信息
     */
    @OperLog(module = "认证管理", action = "注册")
    @PostMapping("/register")
    public R<SysUser> register(@RequestBody Map<String, String> registerBody) {
        SysUser user = userService.register(
                registerBody.get("username"),
                registerBody.get("password")
        );
        return ok(user);
    }

    /**
     * 获取当前登录用户信息（含角色列表）
     *
     * <p>从 SecurityContext 中提取当前登录用户的用户名，查询并返回完整的用户信息
     * 及关联的角色列表。前端可据此判断用户的角色权限和动态路由。</p>
     *
     * @return 包含用户信息和角色列表的 Map
     */
    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo() {
        String username = LoginUser.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        List<SysRole> roles = roleService.selectRolesByUserId(user.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        return ok(result);
    }
}
