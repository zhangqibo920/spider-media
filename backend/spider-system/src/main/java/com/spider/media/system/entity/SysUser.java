package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 用户账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 用户邮箱 */
    private String email;

    /** 手机号码 */
    private String phonenumber;

    /** 用户头像 */
    private String avatar;

    /** 用户密码 */
    @JsonIgnore
    private String password;

    /** 帐号状态（0正常 1停用） */
    private String status;

    /** 角色（USER普通用户 ADMIN管理员） */
    private String role;
}
