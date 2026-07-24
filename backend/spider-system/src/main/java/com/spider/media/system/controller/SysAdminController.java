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

@RestController
@RequestMapping("/api/admin")
public class SysAdminController {

    private final ISysConfigService configService;
    private final ISysUserService userService;
    private final SysOperLogMapper operLogMapper;

    public SysAdminController(ISysConfigService configService,
                               ISysUserService userService,
                               SysOperLogMapper operLogMapper) {
        this.configService = configService;
        this.userService = userService;
        this.operLogMapper = operLogMapper;
    }

    @GetMapping("/config")
    public R<List<SysConfig>> getConfig(@RequestParam(value = "group") String group) {
        return R.ok(configService.selectConfigList(group));
    }

    @PostMapping("/config")
    public R<Void> addConfig(@RequestBody SysConfig config) {
        config.setCreateBy(LoginUser.getUsername());
        configService.insertConfig(config);
        return R.ok();
    }

    @PutMapping("/config")
    public R<Void> updateConfig(@RequestBody SysConfig config) {
        config.setUpdateBy(LoginUser.getUsername());
        configService.updateConfig(config);
        return R.ok();
    }

    @DeleteMapping("/config/{id}")
    public R<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return R.ok();
    }

    @GetMapping("/users")
    public R<List<SysUser>> getUsers() {
        return R.ok(userService.selectUserList());
    }

    @PutMapping("/users")
    public R<Void> updateUser(@RequestBody SysUser user) {
        user.setUpdateBy(LoginUser.getUsername());
        userService.updateUser(user);
        return R.ok();
    }

    @DeleteMapping("/users/{userId}")
    public R<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return R.ok();
    }

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
