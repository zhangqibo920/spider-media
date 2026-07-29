package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置 Mapper 接口
 *
 * <p>定义对 sys_config 表的数据库访问操作，支持按分组查询、按键查询、新增、更新、删除。</p>
 */
@Mapper
public interface SysConfigMapper {

    /**
     * 根据主键ID查询单条配置
     *
     * @param id 配置主键ID
     * @return 配置实体，不存在返回 null
     */
    SysConfig selectById(@Param("id") Long id);

    /**
     * 按配置键前缀查询配置列表（用于获取某个模块下的所有配置）
     *
     * @param configKey 配置键前缀（如 "sys.upload" 匹配所有上传相关配置）
     * @return 配置列表
     */
    List<SysConfig> selectByGroup(@Param("configKey") String configKey);

    /**
     * 根据精确的配置键查询单条配置
     *
     * @param configKey 配置键（精确匹配）
     * @return 配置实体，不存在返回 null
     */
    SysConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 新增系统配置
     *
     * @param config 待插入的配置实体
     * @return 受影响的行数
     */
    int insert(SysConfig config);

    /**
     * 更新系统配置
     *
     * @param config 待更新的配置实体（根据 id 匹配）
     * @return 受影响的行数
     */
    int update(SysConfig config);

    /**
     * 根据主键ID删除配置
     *
     * @param id 配置主键ID
     * @return 受影响的行数
     */
    int deleteById(@Param("id") Long id);
}
