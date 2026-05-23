package com.liluo.moyan.module.notification.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知死信队列消费者（仅记录日志）
 */
@Slf4j
@Component
public class NotificationDlqConsumer {
    
    /**
     * 监听通知死信队列
     */
    @RabbitListener(queues = "notification.dlq")
    public void handle(NotificationEvent event) {
        log.error("【死信队列】通知消息处理失败: receiverUserId={}, type={}, content={}", 
                event.getReceiverUserId(), event.getType(), event.getContent());
        log.error("【死信队列】请检查数据库连接、Redis状态或业务逻辑");
    }
}
