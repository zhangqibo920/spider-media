package com.spider.media.system.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.aspect.OperLog;
import com.spider.media.system.entity.SysConfig;
import com.spider.media.system.entity.SysOperLog;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.service.ISysConfigService;
import com.spider.media.system.service.ISysOperLogService;
import com.spider.media.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 *
 * <p>提供管理员专用的后台管理接口，包括系统配置管理、用户管理、操作日志查询。
 * 所有接口路径在 /api/admin 下，<b>类级别强制要求 ADMIN 角色</b>才能访问。
 * 通过 {@code @PreAuthorize("hasRole('ADMIN')")} 在方法调度前由 Spring Security 拦截校验，
 * 未通过校验返回 403。</p>
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class SysAdminController extends BaseController {

    /** 系统配置业务层服务 */
    private final ISysConfigService configService;
    /** 用户业务层服务 */
    private final ISysUserService userService;
    /** 操作日志业务层服务 */
    private final ISysOperLogService operLogService;

    public SysAdminController(ISysConfigService configService,
                               ISysUserService userService,
                               ISysOperLogService operLogService) {
        this.configService = configService;
        this.userService = userService;
        this.operLogService = operLogService;
    }

    // ========== 配置管理 ==========

    /**
     * 按分组查询配置列表
     *
     * @param group 配置分组（configKey 前缀）
     * @return 该分组下的所有配置
     */
    @OperLog(module = "配置管理", action = "查询")
    @GetMapping("/config")
    public R<List<SysConfig>> getConfig(@RequestParam(value = "group") String group) {
        return ok(configService.selectConfigList(group));
    }

    /**
     * 新增系统配置
     *
     * @param config 待新增的配置实体
     * @return 操作结果
     */
    @OperLog(module = "配置管理", action = "新增")
    @PostMapping("/config")
    public R<Void> addConfig(@Valid @RequestBody SysConfig config) {
        config.setCreateBy(LoginUser.getUsername());
        configService.insertConfig(config);
        return ok();
    }

    /**
     * 更新系统配置
     *
     * @param config 待更新的配置实体（必须包含 id）
     * @return 操作结果
     */
    @OperLog(module = "配置管理", action = "修改")
    @PutMapping("/config")
    public R<Void> updateConfig(@Valid @RequestBody SysConfig config) {
        config.setUpdateBy(LoginUser.getUsername());
        configService.updateConfig(config);
        return ok();
    }

    /**
     * 删除系统配置
     *
     * @param id 配置主键ID
     * @return 操作结果
     */
    @OperLog(module = "配置管理", action = "删除")
    @DeleteMapping("/config/{id}")
    public R<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ok();
    }

    // ========== 用户管理 ==========

    /**
     * 查询所有用户列表
     *
     * @return 用户列表
     */
    @OperLog(module = "用户管理", action = "查询")
    @GetMapping("/users")
    public R<List<SysUser>> getUsers() {
        return ok(userService.selectUserList());
    }

    /**
     * 更新用户信息
     *
     * @param user 待更新的用户实体
     * @return 操作结果
     */
    @OperLog(module = "用户管理", action = "修改")
    @PutMapping("/users")
    public R<Void> updateUser(@RequestBody SysUser user) {
        user.setUpdateBy(LoginUser.getUsername());
        userService.updateUser(user);
        return ok();
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @OperLog(module = "用户管理", action = "删除")
    @DeleteMapping("/users/{userId}")
    public R<Void> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return ok();
    }

    /**
     * 重置用户密码（管理员操作，无需旧密码）
     *
     * @param request 包含 userId 和 newPassword 的请求体
     * @return 操作结果
     */
    @OperLog(module = "用户管理", action = "重置密码")
    @PutMapping("/users/password")
    public R<Void> resetPassword(@RequestBody java.util.Map<String, String> request) {
        Long userId = Long.valueOf(request.get("userId"));
        String newPassword = request.get("newPassword");
        userService.resetPassword(userId, newPassword);
        return ok();
    }

    // ========== 操作日志 ==========

    /**
     * 分页查询操作日志
     *
     * <p>支持分页查询系统中所有用户的操作日志记录，用于管理员审计追踪。</p>
     *
     * @param page 页码（从 1 开始，默认 1）
     * @param size 每页条数（默认 20）
     * @return 包含日志列表和总数的分页结果
     */
    @OperLog(module = "日志管理", action = "查询")
    @GetMapping("/logs")
    public R<Map<String, Object>> getLogs(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size) {
        List<SysOperLog> logs = operLogService.selectLogPage(null, null, (page - 1) * size, size);
        long total = operLogService.selectLogCount(null, null);
        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        return ok(result);
    }
}
