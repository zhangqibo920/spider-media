package com.spider.media.datacollection.service;

import com.spider.media.datacollection.entity.DcTargetAccount;

import java.util.List;

/**
 * 对标账号业务层接口
 *
 * <p>定义对标账号的查询、新增、删除操作。
 * 由 {@link com.spider.media.datacollection.service.impl.DcTargetAccountServiceImpl} 提供具体实现。</p>
 */
public interface IDcTargetAccountService {

    /**
     * 根据主键ID查询对标账号
     *
     * @param id 对标账号主键ID
     * @return 对标账号实体，不存在返回 null
     */
    DcTargetAccount selectById(Long id);

    /**
     * 查询当前用户的对标账号列表（支持按平台和分组筛选）
     *
     * @param account 包含筛选条件的对标账号实体
     * @return 对标账号列表
     */
    List<DcTargetAccount> selectTargetAccountList(DcTargetAccount account);

    /**
     * 新增对标账号（自动关联当前登录用户）
     *
     * @param account 待新增的对标账号实体
     * @return 受影响的行数
     */
    int insertTargetAccount(DcTargetAccount account);

    /**
     * 逻辑删除对标账号
     *
     * <p>删除前会校验账号归属，仅允许账号所有者删除自己的账号，防止越权删除他人数据。</p>
     *
     * @param id     对标账号主键ID
     * @param userId 当前操作用户ID（用于归属校验）
     * @return 受影响的行数
     * @throws com.spider.media.common.exception.ServiceException 账号不存在或不属于当前用户时抛出
     */
    int deleteTargetAccountById(Long id, Long userId);

    /**
     * 校验对标账号归属（同步方法，供 Controller 在调用 @Async 采集前预校验）
     *
     * <p>校验逻辑：账号必须存在且 userId 等于当前操作用户ID。
     * 不满足时抛出 ServiceException，由全局异常处理器返回 403/404 给前端。</p>
     *
     * @param id     对标账号ID
     * @param userId 当前操作用户ID
     * @return 通过校验的账号实体（供调用方使用，避免再次查询）
     * @throws com.spider.media.common.exception.ServiceException 账号不存在或不属于当前用户时抛出
     */
    DcTargetAccount validateOwnership(Long id, Long userId);

    /**
     * 更新对标账号
     *
     * @param account 包含更新字段的对标账号实体
     * @return 受影响的行数
     */
    int updateTargetAccount(DcTargetAccount account);
}
