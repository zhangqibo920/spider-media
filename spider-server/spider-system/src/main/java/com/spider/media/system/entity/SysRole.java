package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    private Long roleId;

    private String roleName;

    private String roleKey;

    private String status;
}
