package com.liluo.moyan.module.like.service;

import com.liluo.moyan.common.config.RabbitMQConfig;
import com.liluo.moyan.module.work.entity.Work;
import com.liluo.moyan.module.like.mq.LikeEvent;
import com.liluo.moyan.module.notification.mq.NotificationEvent;
import com.liluo.moyan.common.exception.BusinessException;
import com.liluo.moyan.module.work.mapper.WorkMapper;
import com.liluo.moyan.module.user.mapper.UserMapper;
import com.liluo.moyan.module.user.entity.User;
import com.liluo.moyan.common.util.RedisUtil;
import com.liluo.moyan.common.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 点赞服务
 */
@Slf4j
@Service
public class LikeService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private UserMapper userMapper;
    
    private static final String LIKE_SET_PREFIX = "like:work:";
    private static final String LIKE_COUNT_PREFIX = "like:count:";
    
    /**
     * 点赞
     */
    public void like(Long workId) {
        Long userId = UserHolder.getUserId();
        
        String setKey = LIKE_SET_PREFIX + workId;
        String countKey = LIKE_COUNT_PREFIX + workId;
        
        // Redis Set 幂等判重
        boolean added = redisUtil.addToSet(setKey, userId);
        if (!added) {
            throw new BusinessException("已经点赞过");
        }
        
        // Redis String 原子计数
        redisUtil.increment(countKey);
        
        log.info("用户 {} 点赞作品 {}（Redis）", userId, workId);
        
        // 1. 发送 MQ 异步落库
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.LIKE_EXCHANGE,
            RabbitMQConfig.LIKE_ROUTING_KEY,
            new LikeEvent(workId, userId, "LIKE")
        );
        
        // 2. 发送通知（只有新增点赞时才通知）
        sendLikeNotification(userId, workId);
    }
    
    /**
     * 取消点赞（不发通知）
     */
    public void unlike(Long workId) {
        Long userId = UserHolder.getUserId();
        
        String setKey = LIKE_SET_PREFIX + workId;
        String countKey = LIKE_COUNT_PREFIX + workId;
        
        // Redis Set 移除
        Long removed = redisUtil.removeFromSet(setKey, userId);
        if (removed == null || removed == 0) {
            throw new BusinessException("未点赞过，无法取消");
        }
        
        // Redis String 原子减少计数
        redisUtil.decrement(countKey);
        
        log.info("用户 {} 取消点赞作品 {}（Redis）", userId, workId);
        
        // 发送 MQ 异步更新数据库
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.LIKE_EXCHANGE,
            RabbitMQConfig.LIKE_ROUTING_KEY,
            new LikeEvent(workId, userId, "UNLIKE")
        );
    }
    
    /**
     * 查询点赞数
     */
    public Long getLikeCount(Long workId) {
        String key = LIKE_COUNT_PREFIX + workId;
        Object val = redisUtil.get(key);
        if (val == null) return 0L;
        return Long.parseLong(val.toString());
    }
    
    /**
     * 查询是否点赞
     */
    public boolean isLiked(Long workId) {
        Long userId = UserHolder.getUserId();
        String key = LIKE_SET_PREFIX + workId;
        return Boolean.TRUE.equals(redisUtil.isMemberOfSet(key, userId));
    }
    
    /**
     * 发送点赞通知
     */
    private void sendLikeNotification(Long userId, Long workId) {
        try {
            Work work = workMapper.selectById(workId);
            if (work != null && !work.getUserId().equals(userId)) {
                User user = userMapper.selectById(userId);
                String nickname = user != null ? user.getNickname() : "用户" + userId;

                NotificationEvent event = new NotificationEvent();
                event.setReceiverUserId(work.getUserId());
                event.setType("LIKE");
                event.setContent(nickname + " 点赞了你的作品《" + work.getTitle() + "》");
                event.setSourceId(workId);
                event.setOperatorUserId(userId);
                
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
                );
                
                log.info("发送点赞通知: from={}, to={}, workId={}", userId, work.getUserId(), workId);
            }
        } catch (Exception e) {
            log.error("发送点赞通知失败", e);
        }
    }
}
