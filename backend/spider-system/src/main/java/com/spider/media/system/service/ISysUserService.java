package com.spider.media.system.service;

import com.spider.media.system.entity.SysUser;

import java.util.List;

/**
 * 系统用户业务层接口
 *
 * <p>定义用户注册、登录、查询、更新、删除等核心业务方法。
 * 由 {@link com.spider.media.system.service.impl.SysUserServiceImpl} 提供具体实现。</p>
 */
public interface ISysUserService {

    /**
     * 用户注册
     *
     * @param userName 用户登录账号
     * @param password 明文密码（内部会进行 BCrypt 加密存储）
     * @return 注册成功后的用户实体（不含密码）
     */
    SysUser register(String userName, String password);

    /**
     * 用户登录（验证用户名密码并生成 JWT Token）
     *
     * @param userName 用户登录账号
     * @param password 明文密码
     * @return JWT Token 字符串
     */
    String login(String userName, String password);

    /**
     * 根据用户名查询用户
     *
     * @param userName 用户登录账号
     * @return 用户实体，不存在返回 null
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 查询所有用户列表
     *
     * @return 用户列表
     */
    List<SysUser> selectUserList();

    /**
     * 更新用户信息
     *
     * @param user 待更新的用户实体
     * @return 受影响的行数
     */
    int updateUser(SysUser user);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 受影响的行数
     */
    int deleteUser(Long userId);

    /**
     * 修改用户密码
     *
     * @param userId    用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 受影响的行数
     */
    int updatePassword(Long userId, String oldPassword, String newPassword);
}
