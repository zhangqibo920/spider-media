package com.spider.media.system.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.aspect.OperLog;
import com.spider.media.system.entity.SysRole;
import com.spider.media.system.service.ISysRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/role")
@PreAuthorize("hasRole('ADMIN')")
public class SysRoleController extends BaseController {

    private final ISysRoleService roleService;

    public SysRoleController(ISysRoleService roleService) {
        this.roleService = roleService;
    }

    @OperLog(module = "角色管理", action = "查询")
    @GetMapping
    public R<List<SysRole>> list() {
        return ok(roleService.selectRoleList());
    }

    @OperLog(module = "角色管理", action = "查询")
    @GetMapping("/{roleId}")
    public R<SysRole> get(@PathVariable Long roleId) {
        return ok(roleService.selectRoleById(roleId));
    }

    @OperLog(module = "角色管理", action = "新增")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        role.setCreateBy(LoginUser.getUsername());
        roleService.insertRole(role);
        return ok();
    }

    @OperLog(module = "角色管理", action = "修改")
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        role.setUpdateBy(LoginUser.getUsername());
        roleService.updateRole(role);
        return ok();
    }

    @OperLog(module = "角色管理", action = "删除")
    @DeleteMapping("/{roleId}")
    public R<Void> delete(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ok();
    }

    @OperLog(module = "角色管理", action = "查询菜单")
    @GetMapping("/{roleId}/menus")
    public R<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return ok(roleService.selectMenuIdsByRoleId(roleId));
    }

    @OperLog(module = "角色管理", action = "分配菜单")
    @PutMapping("/{roleId}/menus")
    public R<Void> updateMenus(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        roleService.updateRoleMenus(roleId, body.get("menuIds"));
        return ok();
    }
}
