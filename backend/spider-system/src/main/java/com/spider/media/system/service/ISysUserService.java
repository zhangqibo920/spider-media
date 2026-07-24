package com.spider.media.system.service;

import com.spider.media.system.entity.SysUser;

import java.util.List;

public interface ISysUserService {

    SysUser register(String userName, String password);

    String login(String userName, String password);

    SysUser selectUserByUserName(String userName);

    List<SysUser> selectUserList();

    int updateUser(SysUser user);

    int deleteUser(Long userId);
}
