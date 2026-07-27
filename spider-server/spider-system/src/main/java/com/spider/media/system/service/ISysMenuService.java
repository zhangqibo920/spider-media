package com.spider.media.system.service;

import com.spider.media.system.entity.SysMenu;

import java.util.List;

public interface ISysMenuService {

    List<SysMenu> selectMenuList();

    List<SysMenu> selectMenuTreeByUserId(Long userId);

    List<SysMenu> selectMenuTreeByRoleId(Long roleId);

    SysMenu selectMenuById(Long menuId);

    int insertMenu(SysMenu menu);

    int updateMenu(SysMenu menu);

    int deleteMenu(Long menuId);
}
