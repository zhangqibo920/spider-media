package com.spider.media.contentpublish.service;

import com.spider.media.contentpublish.entity.PbPlatformAccount;

import java.util.List;

/**
 * 平台账号Service接口
 */
public interface IPbPlatformAccountService {

    List<PbPlatformAccount> selectAccountList(Long userId);

    int insertAccount(PbPlatformAccount account);

    int deleteAccountById(Long id);
}
