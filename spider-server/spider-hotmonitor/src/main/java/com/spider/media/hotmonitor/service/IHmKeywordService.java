package com.spider.media.hotmonitor.service;

import com.spider.media.hotmonitor.entity.HmKeyword;

import java.util.List;

public interface IHmKeywordService {

    List<HmKeyword> selectList(Long userId);

    HmKeyword selectById(Long id);

    HmKeyword create(HmKeyword keyword, Long userId, String username);

    HmKeyword update(HmKeyword keyword, String username);

    void delete(Long id);

    void toggleStatus(Long id, String status, String username);
}
