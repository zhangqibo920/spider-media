package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysConfigMapper {

    List<SysConfig> selectByGroup(@Param("configKey") String configKey);

    SysConfig selectByKey(@Param("configKey") String configKey);

    int insert(SysConfig config);

    int update(SysConfig config);

    int deleteById(@Param("id") Long id);
}
