package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {

    SysUser selectByUserName(@Param("userName") String userName);

    int insert(SysUser user);

    List<SysUser> selectList();

    int update(SysUser user);

    int deleteById(@Param("userId") Long userId);
}
