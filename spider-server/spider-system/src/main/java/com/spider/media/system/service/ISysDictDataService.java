package com.spider.media.system.service;

import com.spider.media.system.entity.SysDictData;

import java.util.List;

/**
 * 字典数据业务层接口
 */
public interface ISysDictDataService {

    /** 根据字典类型查询字典数据列表 */
    List<SysDictData> selectDictDataByType(String dictType);

    /** 根据字典类型和字典值查询字典数据 */
    SysDictData selectDictDataByTypeAndValue(String dictType, String dictValue);

    /** 根据字典类型和字典值获取字典标签（便捷方法） */
    String getDictLabel(String dictType, String dictValue);

    /** 新增字典数据 */
    int insertDictData(SysDictData dictData);

    /** 更新字典数据 */
    int updateDictData(SysDictData dictData);

    /** 删除字典数据 */
    int deleteDictDataById(Long id);
}
