package com.spider.media.hotmonitor.mapper;

import com.spider.media.hotmonitor.entity.HmNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HmNotificationMapper {

    HmNotification selectById(@Param("id") Long id);

    List<HmNotification> selectByUserId(@Param("userId") Long userId, @Param("isRead") String isRead);

    int countUnread(@Param("userId") Long userId);

    int insert(HmNotification notification);

    int markAsRead(@Param("id") Long id);

    int markAllAsRead(@Param("userId") Long userId);
}
