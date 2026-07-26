package com.spider.media.datacollection.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.datacollection.entity.DcTargetAccount;
import com.spider.media.datacollection.mapper.DcTargetAccountMapper;
import com.spider.media.datacollection.service.IDcTargetAccountService;
import com.spider.media.framework.security.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对标账号业务层实现类
 *
 * <p>实现对标账号的查询、新增、删除操作。
 * 查询和新增时自动关联当前登录用户，确保数据按用户隔离。
 * 删除时强制校验账号归属，防止越权删除他人数据。</p>
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
    @Transactional(rollbackFor = Exception.class)
    public int insertTargetAccount(DcTargetAccount account) {
        Long userId = LoginUser.getUserId();
        account.setUserId(userId);
        account.setCreateBy(LoginUser.getUsername());
        account.setCreateTime(LocalDateTime.now());
        return targetAccountMapper.insert(account);
    }

    /**
     * 逻辑删除对标账号
     *
     * <p>删除前校验账号归属：账号必须存在且 userId 等于当前操作用户ID。
     * 不满足时抛出 ServiceException，防止越权删除他人数据。</p>
     *
     * @param id     对标账号ID
     * @param userId 当前操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTargetAccountById(Long id, Long userId) {
        validateOwnership(id, userId);
        return targetAccountMapper.deleteById(id);
    }

    /**
     * 校验对标账号归属
     *
     * <p>统一校验入口，供删除、采集等操作复用。
     * 账号不存在返回 404，账号不属于当前用户返回 403。</p>
     */
    @Override
    public DcTargetAccount validateOwnership(Long id, Long userId) {
        DcTargetAccount account = targetAccountMapper.selectById(id);
        if (account == null) {
            throw new ServiceException(ErrorCodeEnums.DC_TARGET_ACCOUNT_NOT_FOUND);
        }
        if (userId == null || !account.getUserId().equals(userId)) {
            throw new ServiceException(ErrorCodeEnums.FORBIDDEN, "无权操作他人的对标账号");
        }
        return account;
    }
}
