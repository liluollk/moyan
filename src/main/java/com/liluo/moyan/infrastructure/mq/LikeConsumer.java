package com.liluo.moyan.infrastructure.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.liluo.moyan.modules.like.entity.UserLike;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.infrastructure.mq.LikeEvent;
import com.liluo.moyan.modules.like.mapper.UserLikeMapper;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞消费者（异步落库）
 */
@Slf4j
@Component
public class LikeConsumer {
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Autowired
    private WorkMapper workMapper;
    
    /**
     * 监听点赞队列
     * 
     * 重试机制：
     * - 第1次失败后，等待1秒重试
     * - 第2次失败后，等待2秒重试
     * - 第3次失败后，进入死信队列
     */
    @RabbitListener(queues = "like.queue")
    @Transactional(rollbackFor = Exception.class)
    public void handle(LikeEvent event, Channel channel, Message message) {
        try {
            log.info("收到点赞事件: workId={}, userId={}, action={}", 
                    event.getWorkId(), event.getUserId(), event.getAction());
            
            if ("LIKE".equals(event.getAction())) {
                // 点赞：插入记录 + 增加计数
                UserLike userLike = new UserLike();
                userLike.setUserId(event.getUserId());
                userLike.setWorkId(event.getWorkId());
                userLikeMapper.insert(userLike);
                
                workMapper.update(null,
                    new LambdaUpdateWrapper<Work>()
                        .eq(Work::getId, event.getWorkId())
                        .setSql("like_count = like_count + 1")
                );
                
                log.info("点赞落库成功: userId={}, workId={}", event.getUserId(), event.getWorkId());
                
            } else if ("UNLIKE".equals(event.getAction())) {
                // 取消点赞：删除记录 + 减少计数
                userLikeMapper.delete(
                    new LambdaQueryWrapper<UserLike>()
                        .eq(UserLike::getUserId, event.getUserId())
                        .eq(UserLike::getWorkId, event.getWorkId())
                );
                
                workMapper.update(null,
                    new LambdaUpdateWrapper<Work>()
                        .eq(Work::getId, event.getWorkId())
                        .setSql("like_count = GREATEST(like_count - 1, 0)")
                );
                
                log.info("取消点赞落库成功: userId={}, workId={}", event.getUserId(), event.getWorkId());
            }
            
            // 手动确认
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                log.error("ACK失败", e);
            }
            
        } catch (Exception e) {
            log.error("点赞处理失败，将自动重试", e);
            // 抛出异常，触发 Spring AMQP 的自动重试机制
            // 重试3次后仍失败，会自动进入死信队列
            throw e;
        }
    }
}
