package com.spider.media.hotmonitor.service;

import com.spider.media.hotmonitor.entity.HmNotification;

import java.util.List;

public interface IHmNotificationService {

    List<HmNotification> selectList(Long userId, String isRead);

    int countUnread(Long userId);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);
}
