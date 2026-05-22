package com.liluo.moyan.modules.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.notification.entity.Notification;
import com.liluo.moyan.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统通知控制器
 */
@Tag(name = "系统通知")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Operation(summary = "获取未读通知数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Integer count = notificationService.getUnreadCount();
        return Result.success(count);
    }
    
    @Operation(summary = "获取通知列表（浏览即已读）")
    @GetMapping
    public Result<Page<Notification>> getNotificationList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Notification> page = notificationService.getNotificationList(pageNum, pageSize);
        return Result.success(page);
    }
}
