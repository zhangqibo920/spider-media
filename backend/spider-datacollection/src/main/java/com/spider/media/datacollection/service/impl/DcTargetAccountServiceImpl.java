package com.spider.media.datacollection.service.impl;

import com.spider.media.datacollection.entity.DcTargetAccount;
import com.spider.media.datacollection.mapper.DcTargetAccountMapper;
import com.spider.media.datacollection.service.IDcTargetAccountService;
import com.spider.media.framework.security.LoginUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对标账号Service实现
 */
@Service
public class DcTargetAccountServiceImpl implements IDcTargetAccountService {

    private final DcTargetAccountMapper targetAccountMapper;

    public DcTargetAccountServiceImpl(DcTargetAccountMapper targetAccountMapper) {
        this.targetAccountMapper = targetAccountMapper;
    }

    @Override
    public List<DcTargetAccount> selectTargetAccountList(DcTargetAccount account) {
        Long userId = LoginUser.getUserId();
        return targetAccountMapper.selectList(userId, account.getPlatform(), account.getGroupName());
    }

    @Override
    public int insertTargetAccount(DcTargetAccount account) {
        Long userId = LoginUser.getUserId();
        account.setUserId(userId);
        account.setCreateBy(LoginUser.getUsername());
        account.setCreateTime(LocalDateTime.now());
        return targetAccountMapper.insert(account);
    }

    @Override
    public int deleteTargetAccountById(Long id) {
        return targetAccountMapper.deleteById(id);
    }
}
