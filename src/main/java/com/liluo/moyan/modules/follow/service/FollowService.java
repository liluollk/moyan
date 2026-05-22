package com.liluo.moyan.modules.follow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.infrastructure.mq.RabbitMQConfig;
import com.liluo.moyan.modules.follow.entity.Follow;
import com.liluo.moyan.infrastructure.mq.NotificationEvent;
import com.liluo.moyan.framework.exception.BusinessException;
import com.liluo.moyan.modules.follow.mapper.FollowMapper;
import com.liluo.moyan.infrastructure.user.mapper.UserMapper;
import com.liluo.moyan.infrastructure.user.entity.User;
import com.liluo.moyan.framework.security.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关注服务
 */
@Slf4j
@Service
public class FollowService {
    
    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 关注用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followedUserId) {
        Long userId = UserHolder.getUserId();
        
        if (userId.equals(followedUserId)) {
            throw new BusinessException("不能关注自己");
        }
        
        // 检查是否已关注
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowedUserId, followedUserId);
        
        Follow existFollow = followMapper.selectOne(wrapper);
        
        if (existFollow != null) {
            if (existFollow.getStatus() == 0) {
                throw new BusinessException("已经关注过");
            }
            // 重新关注
            existFollow.setStatus(0);
            followMapper.updateById(existFollow);
        } else {
            // 新增关注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowedUserId(followedUserId);
            follow.setStatus(0);
            followMapper.insert(follow);
        }
        
        log.info("用户 {} 关注用户 {}", userId, followedUserId);
        
        // 异步发送通知
        try {
            User user = userMapper.selectById(userId);
            String nickname = user != null ? user.getNickname() : "用户" + userId;

            NotificationEvent event = new NotificationEvent();
            event.setReceiverUserId(followedUserId);
            event.setType("FOLLOW");
            event.setContent(nickname + " 关注了你");
            event.setOperatorUserId(userId);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                event
            );
            log.info("发送关注通知: from={}, to={}", userId, followedUserId);
        } catch (Exception e) {
            log.error("发送关注通知失败", e);
        }
    }
    
    /**
     * 取消关注
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long followedUserId) {
        Long userId = UserHolder.getUserId();
        
        // 查询关注记录
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowedUserId, followedUserId);
        
        Follow follow = followMapper.selectOne(wrapper);
        if (follow != null) {
            follow.setStatus(1);
            followMapper.updateById(follow);
        }
        
        log.info("用户 {} 取消关注用户 {}", userId, followedUserId);
    }
    
    /**
     * 检查是否关注
     */
    public boolean isFollowing(Long followedUserId) {
        Long userId = UserHolder.getUserId();
        
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowedUserId, followedUserId)
                .eq(Follow::getStatus, 0);
        
        return followMapper.selectCount(wrapper) > 0;
    }
}
