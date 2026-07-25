package com.spider.media.system.service.impl;

import com.spider.media.system.entity.SysDictType;
import com.spider.media.system.mapper.SysDictDataMapper;
import com.spider.media.system.mapper.SysDictTypeMapper;
import com.spider.media.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典类型业务层实现类
 */
@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public SysDictTypeServiceImpl(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public List<SysDictType> selectDictTypeList() {
        return dictTypeMapper.selectAll();
    }

    @Override
    public SysDictType selectByDictType(String dictType) {
        return dictTypeMapper.selectByDictType(dictType);
    }

    @Override
    public int insertDictType(SysDictType dictType) {
        dictType.setStatus("0");
        dictType.setCreateTime(LocalDateTime.now());
        return dictTypeMapper.insert(dictType);
    }

    @Override
    public int updateDictType(SysDictType dictType) {
        dictType.setUpdateTime(LocalDateTime.now());
        return dictTypeMapper.update(dictType);
    }

    /**
     * 删除字典类型（同时删除其下的所有字典数据）
     */
    @Override
    @Transactional
    public int deleteDictTypeById(Long id) {
        SysDictType dictType = dictTypeMapper.selectByDictType(null);
        // 先查出 dictType 标识，再删除关联的字典数据
        if (dictType != null) {
            dictDataMapper.deleteByDictType(dictType.getDictType());
        }
        return dictTypeMapper.deleteById(id);
    }
}
