package com.spider.media.datacollection.service;

import com.spider.media.datacollection.entity.DcTargetAccount;

import java.util.List;

/**
 * 对标账号Service接口
 */
public interface IDcTargetAccountService {

    /**
     * 查询对标账号列表
     */
    List<DcTargetAccount> selectTargetAccountList(DcTargetAccount account);

    /**
     * 新增对标账号
     */
    int insertTargetAccount(DcTargetAccount account);

    /**
     * 删除对标账号
     */
    int deleteTargetAccountById(Long id);
}
