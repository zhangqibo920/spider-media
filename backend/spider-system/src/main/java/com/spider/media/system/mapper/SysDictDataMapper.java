package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据 Mapper 接口
 */
@Mapper
public interface SysDictDataMapper {

    /** 根据字典类型查询字典数据列表（按 sort 排序） */
    List<SysDictData> selectByDictType(@Param("dictType") String dictType);

    /** 根据字典类型和字典值查询 */
    SysDictData selectByDictTypeAndValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    /** 新增字典数据 */
    int insert(SysDictData dictData);

    /** 更新字典数据 */
    int update(SysDictData dictData);

    /** 根据ID删除字典数据 */
    int deleteById(@Param("id") Long id);

    /** 根据字典类型删除所有字典数据 */
    int deleteByDictType(@Param("dictType") String dictType);
}
