package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体类
 *
 * <p>对应数据库表 sys_user，存储系统用户的基本信息，包括账号、密码、角色等。
 * 密码字段使用 @JsonIgnore 注解确保在 JSON 序列化（API 响应）时不返回密码信息。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    /** 用户主键ID，自增 */
    private Long userId;

    /** 用户登录账号（唯一） */
    private String userName;

    /** 用户显示昵称 */
    private String nickName;

    /** 用户邮箱地址 */
    private String email;

    /** 用户手机号码 */
    private String phonenumber;

    /** 用户头像 URL */
    private String avatar;

    /** 用户密码（BCrypt 加密存储，API 响应中不返回） */
    @JsonIgnore
    private String password;

    /** 帐号状态：'0'=正常可用，'1'=停用 */
    private String status;

    /** 用户角色：'USER'=普通用户，'ADMIN'=管理员 */
    private String role;
}
