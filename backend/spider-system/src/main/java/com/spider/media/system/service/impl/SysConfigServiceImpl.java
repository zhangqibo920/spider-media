package com.spider.media.system.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.system.entity.SysConfig;
import com.spider.media.system.mapper.SysConfigMapper;
import com.spider.media.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysConfigServiceImpl implements ISysConfigService {

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

    @Override
    public int insertConfig(SysConfig config) {
        if (configMapper.selectByKey(config.getConfigKey()) != null) {
            throw new ServiceException("配置键已存在");
        }
        config.setCreateTime(LocalDateTime.now());
        return configMapper.insert(config);
    }

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
