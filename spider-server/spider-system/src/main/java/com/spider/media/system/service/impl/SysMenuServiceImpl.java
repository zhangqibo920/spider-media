package com.spider.media.system.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysMenu;
import com.spider.media.system.entity.SysRole;
import com.spider.media.system.mapper.SysMenuMapper;
import com.spider.media.system.service.ISysMenuService;
import com.spider.media.system.service.ISysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl implements ISysMenuService {

    private final SysMenuMapper menuMapper;
    private final ISysRoleService roleService;

    public SysMenuServiceImpl(SysMenuMapper menuMapper, ISysRoleService roleService) {
        this.menuMapper = menuMapper;
        this.roleService = roleService;
    }

    @Override
    public List<SysMenu> selectMenuList() {
        return buildTree(menuMapper.selectList());
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = roles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        List<SysMenu> menus = menuMapper.selectByRoleIds(roleIds);
        return buildTree(menus);
    }

    @Override
    public List<SysMenu> selectMenuTreeByRoleId(Long roleId) {
        List<SysRole> roles = List.of(roleService.selectRoleById(roleId));
        List<Long> roleIds = roles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        List<SysMenu> menus = menuMapper.selectByRoleIds(roleIds);
        return buildTree(menus);
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        return menuMapper.selectById(menuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMenu(SysMenu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        menu.setCreateBy(LoginUser.getUsername());
        menu.setCreateTime(LocalDateTime.now());
        return menuMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMenu(SysMenu menu) {
        if (menu.getMenuId() == null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "菜单ID不能为空");
        }
        menu.setUpdateBy(LoginUser.getUsername());
        menu.setUpdateTime(LocalDateTime.now());
        return menuMapper.update(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMenu(Long menuId) {
        if (menuMapper.selectCountByParentId(menuId) > 0) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "存在子菜单，不允许删除");
        }
        return menuMapper.deleteById(menuId);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                menu.setChildren(findChildren(menu.getMenuId(), menus));
                tree.add(menu);
            }
        }
        tree.sort(Comparator.comparingInt(SysMenu::getSortOrder));
        return tree;
    }

    private List<SysMenu> findChildren(Long parentId, List<SysMenu> menus) {
        List<SysMenu> children = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(findChildren(menu.getMenuId(), menus));
                children.add(menu);
            }
        }
        children.sort(Comparator.comparingInt(SysMenu::getSortOrder));
        return children;
    }
}
