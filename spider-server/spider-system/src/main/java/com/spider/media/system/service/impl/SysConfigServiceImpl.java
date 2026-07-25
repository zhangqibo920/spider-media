package com.spider.media.system.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.system.entity.SysConfig;
import com.spider.media.system.mapper.SysConfigMapper;
import com.spider.media.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统配置业务层实现类
 *
 * <p>实现系统配置的增删查改操作。新增时会检查配置键的唯一性，
 * 防止重复键导致配置覆盖问题。</p>
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService {

    /** 配置数据访问对象 */
    private final SysConfigMapper configMapper;

    public SysConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public List<SysConfig> selectConfigList(String group) {
        return configMapper.selectByGroup(group);
    }

    @Override
    public SysConfig selectConfigByKey(String configKey) {
        return configMapper.selectByKey(configKey);
    }

    /**
     * 根据配置键查询配置值
     */
    @Override
    public String getConfigValueByKey(String configKey) {
        SysConfig config = configMapper.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置键查询配置值（带默认值）
     */
    @Override
    public String getConfigValueByKey(String configKey, String defaultValue) {
        String value = getConfigValueByKey(configKey);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    /**
     * 新增配置（检查配置键唯一性）
     *
     * @throws ServiceException 配置键已存在时抛出异常
     */
    @Override
    public int insertConfig(SysConfig config) {
        if (configMapper.selectByKey(config.getConfigKey()) != null) {
            throw new ServiceException("配置键已存在");
        }
        config.setCreateTime(LocalDateTime.now());
        return configMapper.insert(config);
    }

    /**
     * 更新配置（自动填充更新时间）
     */
    @Override
    public int updateConfig(SysConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        return configMapper.update(config);
    }

    @Override
    public int deleteConfig(Long id) {
        return configMapper.deleteById(id);
    }
}
