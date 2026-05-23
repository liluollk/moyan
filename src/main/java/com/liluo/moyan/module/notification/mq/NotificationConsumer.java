package com.liluo.moyan.module.notification.mq;

import com.liluo.moyan.module.notification.entity.Notification;
import com.liluo.moyan.module.notification.mapper.NotificationMapper;
import com.liluo.moyan.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通知消费者
 */
@Slf4j
@Component
public class NotificationConsumer {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 监听通知队列
     * 
     * 重试机制：
     * - 第1次失败后，等待1秒重试
     * - 第2次失败后，等待2秒重试
     * - 第3次失败后，进入死信队列
     */
    @RabbitListener(queues = "notification.queue")
    public void handle(NotificationEvent event) {
        log.info("收到通知事件: userId={}, type={}", event.getReceiverUserId(), event.getType());

        // 1. 写入通知表
        Notification notification = new Notification();
        notification.setUserId(event.getReceiverUserId());
        notification.setType(convertTypeToInt(event.getType()));
        notification.setContent(event.getContent());
        notification.setIsRead(0);  // 未读

        notificationMapper.insert(notification);

        // 2. 缓存未读数（Redis）
        String unreadKey = "notify:unread:" + event.getReceiverUserId();
        redisUtil.increment(unreadKey);

        log.info("通知写入成功: userId={}, notificationId={}",
                event.getReceiverUserId(), notification.getId());
    }
    
    /**
     * 将通知类型字符串转换为整数
     */
    private Integer convertTypeToInt(String type) {
        return switch (type) {
            case "LIKE" -> 1;
            case "FAVORITE" -> 2;
            case "COMMENT" -> 3;
            case "FOLLOW" -> 4;
            default -> {
                log.warn("未知的通知类型: {}", type);
                yield 0;
            }
        };
    }
}
