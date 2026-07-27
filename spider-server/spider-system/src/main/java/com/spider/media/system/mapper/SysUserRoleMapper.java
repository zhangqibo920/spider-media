package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    List<SysUserRole> selectByUserId(@Param("userId") Long userId);

    int insert(SysUserRole userRole);

    int deleteByUserId(@Param("userId") Long userId);
}
