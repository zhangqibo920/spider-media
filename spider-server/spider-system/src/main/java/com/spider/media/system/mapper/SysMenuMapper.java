package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper {

    List<SysMenu> selectList();

    List<SysMenu> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    SysMenu selectById(@Param("menuId") Long menuId);

    int insert(SysMenu menu);

    int update(SysMenu menu);

    int deleteById(@Param("menuId") Long menuId);

    int selectCountByParentId(@Param("parentId") Long parentId);
}
