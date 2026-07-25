package com.spider.media.system.service;

import com.spider.media.system.entity.SysDictType;

import java.util.List;

/**
 * 字典类型业务层接口
 */
public interface ISysDictTypeService {

    /** 查询所有字典类型 */
    List<SysDictType> selectDictTypeList();

    /** 根据字典类型标识查询 */
    SysDictType selectByDictType(String dictType);

    /** 新增字典类型 */
    int insertDictType(SysDictType dictType);

    /** 更新字典类型 */
    int updateDictType(SysDictType dictType);

    /** 删除字典类型（同时删除其下的所有字典数据） */
    int deleteDictTypeById(Long id);
}
