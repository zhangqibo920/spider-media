package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    List<SysRole> selectList();

    SysRole selectById(@Param("roleId") Long roleId);

    SysRole selectByKey(@Param("roleKey") String roleKey);

    List<SysRole> selectByUserId(@Param("userId") Long userId);

    int insert(SysRole role);

    int update(SysRole role);

    int deleteById(@Param("roleId") Long roleId);
}
