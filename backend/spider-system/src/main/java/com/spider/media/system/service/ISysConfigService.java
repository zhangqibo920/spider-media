package com.spider.media.system.service;

import com.spider.media.system.entity.SysConfig;

import java.util.List;

public interface ISysConfigService {

    List<SysConfig> selectConfigList(String group);

    SysConfig selectConfigByKey(String configKey);

    int insertConfig(SysConfig config);

    int updateConfig(SysConfig config);

    int deleteConfig(Long id);
}
