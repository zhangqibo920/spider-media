package com.spider.media.system.controller;

import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysConfig;
import com.spider.media.system.service.ISysConfigService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统管理Controller
 */
@RestController
@RequestMapping("/api/admin")
public class SysAdminController {

    private final ISysConfigService configService;

    public SysAdminController(ISysConfigService configService) {
        this.configService = configService;
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

    @GetMapping("/logs")
    public R<List<Object>> getLogs(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        return R.ok(List.of());
    }
}
