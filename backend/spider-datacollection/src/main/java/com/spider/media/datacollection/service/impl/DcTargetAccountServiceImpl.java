package com.spider.media.datacollection.service.impl;

import com.spider.media.datacollection.entity.DcTargetAccount;
import com.spider.media.datacollection.mapper.DcTargetAccountMapper;
import com.spider.media.datacollection.service.IDcTargetAccountService;
import com.spider.media.framework.security.LoginUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对标账号业务层实现类
 *
 * <p>实现对标账号的查询、新增、删除操作。
 * 查询和新增时自动关联当前登录用户，确保数据按用户隔离。</p>
 */
@Service
public class DcTargetAccountServiceImpl implements IDcTargetAccountService {

    /** 对标账号数据访问对象 */
    private final DcTargetAccountMapper targetAccountMapper;

    public DcTargetAccountServiceImpl(DcTargetAccountMapper targetAccountMapper) {
        this.targetAccountMapper = targetAccountMapper;
    }

    /**
     * 查询当前用户的对标账号列表
     *
     * <p>自动从 SecurityContext 获取当前用户ID，确保只能查看自己的对标账号。</p>
     */
    @Override
    public List<DcTargetAccount> selectTargetAccountList(DcTargetAccount account) {
        Long userId = LoginUser.getUserId();
        return targetAccountMapper.selectList(userId, account.getPlatform(), account.getGroupName());
    }

    /**
     * 新增对标账号
     *
     * <p>自动设置用户ID、创建人和创建时间。</p>
     */
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
