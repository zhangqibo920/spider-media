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
     * 根据主键ID查询平台账号
     *
     * @param id 账号主键ID
     * @return 平台账号实体，不存在返回 null
     */
    PbPlatformAccount selectById(Long id);

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
     * <p>删除前会校验账号归属，仅允许账号所有者删除，防止越权删除他人数据。</p>
     *
     * @param id     账号主键ID
     * @param userId 当前操作用户ID（用于归属校验）
     * @return 受影响的行数，1表示删除成功
     * @throws com.spider.media.common.exception.ServiceException 账号不存在或不属于当前用户时抛出
     */
    int deleteAccountById(Long id, Long userId);

    /**
     * 校验平台账号归属
     *
     * @param id     账号主键ID
     * @param userId 当前操作用户ID
     * @return 通过校验的账号实体
     * @throws com.spider.media.common.exception.ServiceException 账号不存在或不属于当前用户时抛出
     */
    PbPlatformAccount validateOwnership(Long id, Long userId);

    /**
     * 更新平台账号
     *
     * @param account 待更新的账号实体
     * @return 受影响的行数
     */
    int updateAccount(PbPlatformAccount account);
}
