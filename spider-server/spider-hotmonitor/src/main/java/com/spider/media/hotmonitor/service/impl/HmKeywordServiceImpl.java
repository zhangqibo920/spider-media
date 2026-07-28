package com.spider.media.hotmonitor.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.hotmonitor.entity.HmKeyword;
import com.spider.media.hotmonitor.mapper.HmKeywordMapper;
import com.spider.media.hotmonitor.service.IHmKeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HmKeywordServiceImpl implements IHmKeywordService {

    private final HmKeywordMapper keywordMapper;

    public HmKeywordServiceImpl(HmKeywordMapper keywordMapper) {
        this.keywordMapper = keywordMapper;
    }

    @Override
    public List<HmKeyword> selectList(Long userId) {
        return keywordMapper.selectByUserId(userId);
    }

    @Override
    public HmKeyword selectById(Long id) {
        return keywordMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HmKeyword create(HmKeyword keyword, Long userId, String username) {
        keyword.setUserId(userId);
        keyword.setStatus("0");
        keyword.setCreateBy(username);
        keyword.setCreateTime(LocalDateTime.now());
        keywordMapper.insert(keyword);
        return keyword;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HmKeyword update(HmKeyword keyword, String username) {
        HmKeyword existing = keywordMapper.selectById(keyword.getId());
        if (existing == null) {
            throw new ServiceException(ErrorCodeEnums.HM_KEYWORD_NOT_FOUND);
        }
        keyword.setUpdateBy(username);
        keyword.setUpdateTime(LocalDateTime.now());
        keywordMapper.updateById(keyword);
        return keywordMapper.selectById(keyword.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        keywordMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, String status, String username) {
        HmKeyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            throw new ServiceException(ErrorCodeEnums.HM_KEYWORD_NOT_FOUND);
        }
        keyword.setStatus(status);
        keyword.setUpdateBy(username);
        keyword.setUpdateTime(LocalDateTime.now());
        keywordMapper.updateById(keyword);
    }
}
