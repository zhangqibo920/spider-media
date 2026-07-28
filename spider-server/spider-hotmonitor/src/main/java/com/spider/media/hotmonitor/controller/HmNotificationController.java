package com.spider.media.hotmonitor.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.hotmonitor.entity.HmNotification;
import com.spider.media.hotmonitor.service.IHmNotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotmonitor/notification")
public class HmNotificationController extends BaseController {

    private final IHmNotificationService notificationService;

    public HmNotificationController(IHmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/list")
    public R<List<HmNotification>> list(@RequestParam(required = false) String isRead) {
        Long userId = LoginUser.getUserId();
        return list(notificationService.selectList(userId, isRead));
    }

    @GetMapping("/unread-count")
    public R<Map<String, Integer>> unreadCount() {
        Long userId = LoginUser.getUserId();
        int count = notificationService.countUnread(userId);
        return ok(Map.of("count", count));
    }

    @PutMapping("/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ok();
    }

    @PutMapping("/read-all")
    public R<Void> markAllAsRead() {
        Long userId = LoginUser.getUserId();
        notificationService.markAllAsRead(userId);
        return ok();
    }
}
