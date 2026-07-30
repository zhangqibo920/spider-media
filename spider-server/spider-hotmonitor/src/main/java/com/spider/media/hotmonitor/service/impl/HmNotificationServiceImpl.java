package com.spider.media.hotmonitor.service.impl;

import com.spider.media.hotmonitor.entity.HmNotification;
import com.spider.media.hotmonitor.mapper.HmNotificationMapper;
import com.spider.media.hotmonitor.service.IHmNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HmNotificationServiceImpl implements IHmNotificationService {

    private final HmNotificationMapper notificationMapper;

    public HmNotificationServiceImpl(HmNotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<HmNotification> selectList(Long userId, String isRead) {
        return notificationMapper.selectByUserId(userId, isRead);
    }

    @Override
    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }
}
