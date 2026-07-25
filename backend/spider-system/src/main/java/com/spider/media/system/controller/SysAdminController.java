package com.spider.media.system.controller;

import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysConfig;
import com.spider.media.system.entity.SysOperLog;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.mapper.SysOperLogMapper;
import com.spider.media.system.service.ISysConfigService;
import com.spider.media.system.service.ISysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 *
 * <p>提供管理员专用的后台管理接口，包括系统配置管理、用户管理、操作日志查询。
 * 所有接口路径在 /api/admin 下，需要管理员权限才能访问。</p>
 */
@RestController
@RequestMapping("/api/admin")
public class SysAdminController {

    /** 系统配置业务层服务 */
    private final ISysConfigService configService;
    /** 用户业务层服务 */
    private final ISysUserService userService;
    /** 操作日志数据访问对象 */
    private final SysOperLogMapper operLogMapper;

    public SysAdminController(ISysConfigService configService,
                               ISysUserService userService,
                               SysOperLogMapper operLogMapper) {
        this.configService = configService;
        this.userService = userService;
        this.operLogMapper = operLogMapper;
    }

    // ========== 配置管理 ==========

    /**
     * 按分组查询配置列表
     *
     * @param group 配置分组（configKey 前缀）
     * @return 该分组下的所有配置
     */
    @GetMapping("/config")
    public R<List<SysConfig>> getConfig(@RequestParam(value = "group") String group) {
        return R.ok(configService.selectConfigList(group));
    }

    /**
     * 新增系统配置
     *
     * @param config 待新增的配置实体
     * @return 操作结果
     */
    @PostMapping("/config")
    public R<Void> addConfig(@RequestBody SysConfig config) {
        config.setCreateBy(LoginUser.getUsername());
        configService.insertConfig(config);
        return R.ok();
    }

    /**
     * 更新系统配置
     *
     * @param config 待更新的配置实体（必须包含 id）
     * @return 操作结果
     */
    @PutMapping("/config")
    public R<Void> updateConfig(@RequestBody SysConfig config) {
        config.setUpdateBy(LoginUser.getUsername());
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 删除系统配置
     *
     * @param id 配置主键ID
     * @return 操作结果
     */
    @DeleteMapping("/config/{id}")
    public R<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return R.ok();
    }

    // ========== 用户管理 ==========

    /**
     * 查询所有用户列表
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public R<List<SysUser>> getUsers() {
        return R.ok(userService.selectUserList());
    }

    /**
     * 更新用户信息
     *
     * @param user 待更新的用户实体
     * @return 操作结果
     */
    @PutMapping("/users")
    public R<Void> updateUser(@RequestBody SysUser user) {
        user.setUpdateBy(LoginUser.getUsername());
        userService.updateUser(user);
        return R.ok();
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/users/{userId}")
    public R<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return R.ok();
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
    @GetMapping("/logs")
    public R<Map<String, Object>> getLogs(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size) {
        List<SysOperLog> logs = operLogMapper.selectPage(null, null, (page - 1) * size, size);
        long total = operLogMapper.selectCount(null, null);
        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        return R.ok(result);
    }
}
