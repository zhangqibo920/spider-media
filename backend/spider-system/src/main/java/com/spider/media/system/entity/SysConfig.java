package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体类
 *
 * <p>对应数据库表 sys_config，以键值对形式存储系统运行时的可配置参数。
 * 支持按分组（configKey）查询配置列表，也可通过单个键查询配置值。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity {

    /** 配置主键ID */
    private Long id;

    /** 配置名称（可读的描述信息） */
    private String configName;

    /** 配置键（唯一标识，如 "sys.upload.maxSize"） */
    private String configKey;

    /** 配置值（字符串类型，业务层按需转换类型） */
    private String configValue;

    /** 是否为系统内置配置：'Y'=是（不可删除），'N'=否（可修改/删除） */
    private String configType;
}
