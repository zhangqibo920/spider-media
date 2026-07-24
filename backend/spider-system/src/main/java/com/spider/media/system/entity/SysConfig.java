package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity {

    /** 配置ID */
    private Long id;

    /** 配置名称 */
    private String configName;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 系统内置（Y是 N否） */
    private String configType;
}
