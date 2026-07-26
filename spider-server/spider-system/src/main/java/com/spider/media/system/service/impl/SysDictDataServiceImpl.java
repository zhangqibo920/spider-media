package com.spider.media.system.service.impl;

import com.spider.media.system.entity.SysDictData;
import com.spider.media.system.mapper.SysDictDataMapper;
import com.spider.media.system.service.ISysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典数据业务层实现类
 */
@Service
public class SysDictDataServiceImpl implements ISysDictDataService {

    private final SysDictDataMapper dictDataMapper;

    public SysDictDataServiceImpl(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectByDictType(dictType);
    }

    @Override
    public SysDictData selectDictDataByTypeAndValue(String dictType, String dictValue) {
        return dictDataMapper.selectByDictTypeAndValue(dictType, dictValue);
    }

    /**
     * 根据字典类型和值获取字典标签
     *
     * @return 字典标签，未找到返回原始值
     */
    @Override
    public String getDictLabel(String dictType, String dictValue) {
        SysDictData data = dictDataMapper.selectByDictTypeAndValue(dictType, dictValue);
        return data != null ? data.getDictLabel() : dictValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDictData(SysDictData dictData) {
        dictData.setCreateTime(LocalDateTime.now());
        return dictDataMapper.insert(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDictData(SysDictData dictData) {
        dictData.setUpdateTime(LocalDateTime.now());
        return dictDataMapper.update(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDictDataById(Long id) {
        return dictDataMapper.deleteById(id);
    }
}
