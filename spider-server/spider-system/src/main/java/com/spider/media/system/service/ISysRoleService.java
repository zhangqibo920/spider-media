package com.spider.media.system.service;

import com.spider.media.system.entity.SysRole;

import java.util.List;

public interface ISysRoleService {

    List<SysRole> selectRoleList();

    SysRole selectRoleById(Long roleId);

    List<SysRole> selectRolesByUserId(Long userId);

    int insertRole(SysRole role);

    int updateRole(SysRole role);

    int deleteRole(Long roleId);

    List<Long> selectMenuIdsByRoleId(Long roleId);

    int updateRoleMenus(Long roleId, List<Long> menuIds);
}
