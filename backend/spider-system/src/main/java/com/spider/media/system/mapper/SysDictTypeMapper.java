package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型 Mapper 接口
 */
@Mapper
public interface SysDictTypeMapper {

    /** 查询所有字典类型 */
    List<SysDictType> selectAll();

    /** 根据字典类型标识查询 */
    SysDictType selectByDictType(@Param("dictType") String dictType);

    /** 新增字典类型 */
    int insert(SysDictType dictType);

    /** 更新字典类型 */
    int update(SysDictType dictType);

    /** 根据ID删除字典类型 */
    int deleteById(@Param("id") Long id);
}
