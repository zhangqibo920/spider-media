package com.spider.media.contentpublish.service.impl;

import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.mapper.PbPlatformAccountMapper;
import com.spider.media.contentpublish.service.IPbPlatformAccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台账号Service实现
 */
@Service
public class PbPlatformAccountServiceImpl implements IPbPlatformAccountService {

    private final PbPlatformAccountMapper platformAccountMapper;

    public PbPlatformAccountServiceImpl(PbPlatformAccountMapper platformAccountMapper) {
        this.platformAccountMapper = platformAccountMapper;
    }

    @Override
    public List<PbPlatformAccount> selectAccountList(Long userId) {
        return platformAccountMapper.selectByUserId(userId);
    }

    @Override
    public int insertAccount(PbPlatformAccount account) {
        account.setCreateBy(String.valueOf(account.getUserId()));
        account.setCreateTime(LocalDateTime.now());
        return platformAccountMapper.insert(account);
    }

    @Override
    public int deleteAccountById(Long id) {
        return platformAccountMapper.deleteById(id);
    }
}
