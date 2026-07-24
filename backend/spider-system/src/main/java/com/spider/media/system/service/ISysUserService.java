package com.spider.media.system.service;

import com.spider.media.system.entity.SysUser;

/**
 * 用户Service接口
 */
public interface ISysUserService {

    /**
     * 注册用户
     */
    SysUser register(String userName, String password);

    /**
     * 登录
     */
    String login(String userName, String password);

    /**
     * 根据用户名查询用户
     */
    SysUser selectUserByUserName(String userName);
}
