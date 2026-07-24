package com.spider.media.datacollection.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对标账号表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DcTargetAccount extends BaseEntity {

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

    /** 账号链接 */
    private String accountUrl;

    /** 分组名称 */
    private String groupName;

    /** 状态（0正常 1停用） */
    private String status;

    /** 描述 */
    private String description;
}
