package com.liluo.moyan.module.follow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.common.config.RabbitMQConfig;
import com.liluo.moyan.module.follow.entity.Follow;
import com.liluo.moyan.module.notification.mq.NotificationEvent;
import com.liluo.moyan.common.exception.BusinessException;
import com.liluo.moyan.module.follow.mapper.FollowMapper;
import com.liluo.moyan.module.user.mapper.UserMapper;
import com.liluo.moyan.module.user.entity.User;
import com.liluo.moyan.common.interceptor.UserHolder;
import com.liluo.moyan.common.util.RedisUtil;
import com.liluo.moyan.module.follow.vo.FollowUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisUtil redisUtil;
    
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

        // 清除Feed缓存，确保下次加载关注动态时从数据库查询
        redisUtil.delete("feed:inbox:" + userId);

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

    /**
     * 获取粉丝列表（谁关注了 targetUserId）
     */
    public List<FollowUserVO> getFollowers(Long targetUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowedUserId, targetUserId)
                .eq(Follow::getStatus, 0);
        List<Follow> follows = followMapper.selectList(wrapper);

        Long currentUserId = UserHolder.getUserId();
        Set<Long> myFollowingIds = getMyFollowingIds(currentUserId);

        return follows.stream().map(follow -> {
            User user = userMapper.selectById(follow.getUserId());
            if (user == null) return null;
            return FollowUserVO.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .isFollowing(currentUserId != null && myFollowingIds.contains(user.getId()))
                    .build();
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 获取关注列表（targetUserId 关注了谁）
     */
    public List<FollowUserVO> getFollowing(Long targetUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, targetUserId)
                .eq(Follow::getStatus, 0);
        List<Follow> follows = followMapper.selectList(wrapper);

        Long currentUserId = UserHolder.getUserId();
        Set<Long> myFollowingIds = getMyFollowingIds(currentUserId);

        return follows.stream().map(follow -> {
            User user = userMapper.selectById(follow.getFollowedUserId());
            if (user == null) return null;
            return FollowUserVO.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .isFollowing(currentUserId != null && myFollowingIds.contains(user.getId()))
                    .build();
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    private Set<Long> getMyFollowingIds(Long userId) {
        if (userId == null) return Collections.emptySet();
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getStatus, 0);
        return followMapper.selectList(wrapper).stream()
                .map(Follow::getFollowedUserId)
                .collect(Collectors.toSet());
    }
}
