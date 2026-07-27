package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper {

    List<SysRoleMenu> selectByRoleId(@Param("roleId") Long roleId);

    int insert(SysRoleMenu roleMenu);

    int deleteByRoleId(@Param("roleId") Long roleId);

    int batchInsert(@Param("list") List<SysRoleMenu> list);
}
