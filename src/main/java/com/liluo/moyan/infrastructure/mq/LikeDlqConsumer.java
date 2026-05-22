package com.liluo.moyan.infrastructure.mq;

import com.liluo.moyan.infrastructure.mq.LikeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 点赞死信队列消费者（仅记录日志）
 */
@Slf4j
@Component
public class LikeDlqConsumer {
    
    /**
     * 监听点赞死信队列
     */
    @RabbitListener(queues = "like.dlq")
    public void handle(LikeEvent event) {
        log.error("【死信队列】点赞消息处理失败: workId={}, userId={}, action={}", 
                event.getWorkId(), event.getUserId(), event.getAction());
        log.error("【死信队列】请检查数据库连接、Redis状态或业务逻辑");
    }
}
