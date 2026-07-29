package com.spider.media.system.service;

import com.spider.media.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置业务层接口
 *
 * <p>提供系统配置的增删查改操作，支持按分组查询和按配置键查询。
 * 配置值存储在 sys_config 表中，可通过管理后台动态修改。</p>
 */
public interface ISysConfigService {

    /**
     * 根据主键ID查询单条配置
     *
     * @param id 配置主键ID
     * @return 配置实体，不存在返回 null
     */
    SysConfig selectById(Long id);

    /**
     * 按分组查询配置列表
     *
     * @param group 配置分组（configKey 前缀）
     * @return 该分组下的所有配置
     */
    List<SysConfig> selectConfigList(String group);

    /**
     * 根据配置键查询单条配置
     *
     * @param configKey 配置键
     * @return 配置实体，不存在返回 null
     */
    SysConfig selectConfigByKey(String configKey);

    /**
     * 根据配置键查询配置值（便捷方法）
     *
     * @param configKey 配置键
     * @return 配置值字符串，不存在返回 null
     */
    String getConfigValueByKey(String configKey);

    /**
     * 根据配置键查询配置值（带默认值）
     *
     * @param configKey     配置键
     * @param defaultValue  默认值（配置不存在时返回）
     * @return 配置值字符串
     */
    String getConfigValueByKey(String configKey, String defaultValue);

    /**
     * 新增系统配置（会检查配置键是否已存在）
     *
     * @param config 待新增的配置实体
     * @return 受影响的行数
     */
    int insertConfig(SysConfig config);

    /**
     * 更新系统配置
     *
     * @param config 待更新的配置实体
     * @return 受影响的行数
     */
    int updateConfig(SysConfig config);

    /**
     * 删除系统配置
     *
     * @param id 配置主键ID
     * @return 受影响的行数
     */
    int deleteConfig(Long id);
}
