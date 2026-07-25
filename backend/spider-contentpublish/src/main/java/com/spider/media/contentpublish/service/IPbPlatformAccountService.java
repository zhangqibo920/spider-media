package com.spider.media.contentpublish.service;

import com.spider.media.contentpublish.entity.PbPlatformAccount;

import java.util.List;

/**
 * 平台账号业务层接口
 *
 * <p>定义平台账号管理的核心业务方法，包括查询、新增、删除操作。
 * 由 PbPlatformAccountServiceImpl 提供具体实现。</p>
 */
public interface IPbPlatformAccountService {

    /**
     * 查询指定用户的所有平台账号列表
     *
     * @param userId 用户ID
     * @return 该用户关联的平台账号列表
     */
    List<PbPlatformAccount> selectAccountList(Long userId);

    /**
     * 新增一条平台账号记录
     *
     * <p>自动设置创建人（使用用户ID）和创建时间。</p>
     *
     * @param account 待新增的平台账号实体
     * @return 受影响的行数，1表示新增成功
     */
    int insertAccount(PbPlatformAccount account);

    /**
     * 根据ID逻辑删除平台账号
     *
     * @param id 账号主键ID
     * @return 受影响的行数，1表示删除成功
     */
    int deleteAccountById(Long id);
}
