package com.spider.media.system.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysRole;
import com.spider.media.system.entity.SysRoleMenu;
import com.spider.media.system.mapper.SysRoleMapper;
import com.spider.media.system.mapper.SysRoleMenuMapper;
import com.spider.media.system.service.ISysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public SysRoleServiceImpl(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<SysRole> selectRoleList() {
        return roleMapper.selectList();
    }

    @Override
    public SysRole selectRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return roleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role) {
        if (roleMapper.selectByKey(role.getRoleKey()) != null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "角色标识已存在");
        }
        role.setCreateBy(LoginUser.getUsername());
        role.setCreateTime(LocalDateTime.now());
        return roleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRole role) {
        if (role.getRoleId() == null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "角色ID不能为空");
        }
        role.setUpdateBy(LoginUser.getUsername());
        role.setUpdateTime(LocalDateTime.now());
        return roleMapper.update(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRole(Long roleId) {
        return roleMapper.deleteById(roleId);
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectByRoleId(roleId).stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> list = menuIds.stream()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .collect(Collectors.toList());
            return roleMenuMapper.batchInsert(list);
        }
        return 0;
    }
}
