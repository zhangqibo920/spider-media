package com.spider.media.contentpublish.service.impl;

import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.mapper.PbPlatformAccountMapper;
import com.spider.media.contentpublish.service.IPbPlatformAccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台账号业务层实现类
 *
 * <p>实现 IPbPlatformAccountService 接口，负责平台账号的增删查操作。
 * 通过构造器注入 PbPlatformAccountMapper，由 MyBatis 执行实际的数据库操作。
 * 新增账号时自动填充创建人和创建时间等审计字段。</p>
 */
@Service
public class PbPlatformAccountServiceImpl implements IPbPlatformAccountService {

    /** 平台账号数据访问对象，由 Spring 自动注入 */
    private final PbPlatformAccountMapper platformAccountMapper;

    /** 构造器注入 Mapper 依赖 */
    public PbPlatformAccountServiceImpl(PbPlatformAccountMapper platformAccountMapper) {
        this.platformAccountMapper = platformAccountMapper;
    }

    /**
     * 查询指定用户的所有平台账号
     *
     * @param userId 用户ID
     * @return 该用户的平台账号列表，按创建时间倒序
     */
    @Override
    public List<PbPlatformAccount> selectAccountList(Long userId) {
        return platformAccountMapper.selectByUserId(userId);
    }

    /**
     * 新增平台账号
     *
     * <p>在插入数据库前，自动设置审计字段：
     * <ul>
     *   <li>createBy - 创建人，使用用户ID作为标识</li>
     *   <li>createTime - 创建时间，使用当前系统时间</li>
     * </ul></p>
     *
     * @param account 待新增的平台账号实体
     * @return 受影响的行数，1表示新增成功
     */
    @Override
    public int insertAccount(PbPlatformAccount account) {
        account.setCreateBy(String.valueOf(account.getUserId()));
        account.setCreateTime(LocalDateTime.now());
        return platformAccountMapper.insert(account);
    }

    /**
     * 逻辑删除平台账号
     *
     * @param id 账号主键ID
     * @return 受影响的行数，1表示删除成功
     */
    @Override
    public int deleteAccountById(Long id) {
        return platformAccountMapper.deleteById(id);
    }
}
