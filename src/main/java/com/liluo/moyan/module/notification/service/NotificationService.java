package com.liluo.moyan.module.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.module.notification.entity.Notification;
import com.liluo.moyan.module.notification.mapper.NotificationMapper;
import com.liluo.moyan.common.util.RedisUtil;
import com.liluo.moyan.common.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统通知服务
 */
@Slf4j
@Service
public class NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 获取未读通知数
     */
    public Integer getUnreadCount() {
        Long userId = UserHolder.getUserId();
        String unreadKey = "notify:unread:" + userId;
        
        Object count = redisUtil.get(unreadKey);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }
    
    /**
     * 获取通知列表（浏览即已读）
     * 
     * 逻辑：
     * 1. 查询所有通知（按时间倒序）
     * 2. 将未读的通知批量标记为已读
     * 3. 清空 Redis 未读数
     */
    public Page<Notification> getNotificationList(int pageNum, int pageSize) {
        Long userId = UserHolder.getUserId();
        
        // 1. 分页查询所有通知（按时间倒序，最新在前）
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);
        
        // 2. 找出本页中的未读通知
        List<Notification> unreadNotifications = result.getRecords().stream()
                .filter(n -> n.getIsRead() == 0)
                .toList();
        
        if (!unreadNotifications.isEmpty()) {
            // 3. 批量标记为已读
            List<Long> unreadIds = unreadNotifications.stream()
                    .map(Notification::getId)
                    .collect(Collectors.toList());
            
            notificationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Notification>()
                    .in(Notification::getId, unreadIds)
                    .set(Notification::getIsRead, 1)
            );
            
            // 4. 清空 Redis 未读数（因为已经全部标记为已读）
            String unreadKey = "notify:unread:" + userId;
            redisUtil.delete(unreadKey);
            
            log.info("用户 {} 浏览通知，自动标记 {} 条为已读", userId, unreadIds.size());
        }
        
        return result;
    }
}
