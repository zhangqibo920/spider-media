package com.spider.media.contentpublish.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 平台账号表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PbPlatformAccount extends BaseEntity {

    /** 账号ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 平台类型 */
    private String platform;

    /** 账号名称 */
    private String accountName;

    /** 账号ID */
    private String accountId;

    /** Access Token */
    private String accessToken;

    /** Refresh Token */
    private String refreshToken;

    /** Token过期时间 */
    private LocalDateTime tokenExpireTime;

    /** 状态（0正常 1停用） */
    private String status;

    /** 分组名称 */
    private String groupName;
}
