package com.spider.media.system.service;

import com.spider.media.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置业务层接口
 *
 * <p>定义系统配置的增删查改操作。
 * 由 {@link com.spider.media.system.service.impl.SysConfigServiceImpl} 提供具体实现。</p>
 */
public interface ISysConfigService {

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
