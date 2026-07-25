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
     * @param id 对标账号主键ID
     * @return 受影响的行数
     */
    int deleteTargetAccountById(Long id);
}
