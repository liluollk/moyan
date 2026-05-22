package com.liluo.moyan.modules.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.modules.follow.entity.Follow;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.modules.follow.mapper.FollowMapper;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.framework.util.RedisUtil;
import com.liluo.moyan.framework.security.UserHolder;
import com.liluo.moyan.modules.work.service.WorkService;
import com.liluo.moyan.modules.work.vo.WorkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Feed流服务（推拉结合）
 */
@Slf4j
@Service
public class FeedService {
    
    @Autowired
    private WorkMapper workMapper;
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private WorkService workService;
    
    private static final String FEED_INBOX_KEY = "feed:inbox:";  // 收件箱
    private static final int PUSH_THRESHOLD = 10000;  // 推模式阈值（粉丝数）
    
    /**
     * 获取用户Feed流（关注的人的作品）
     */
    public Page<WorkVO> getFollowFeed(int pageNum, int pageSize) {
        Long userId = UserHolder.getUserId();
        
        // 尝试从Redis缓存获取
        String cacheKey = FEED_INBOX_KEY + userId;
        Set<Object> workIds = redisUtil.zReverseRange(cacheKey, (long) (pageNum - 1) * pageSize, (long) pageNum * pageSize - 1);
        
        if (workIds != null && !workIds.isEmpty()) {
            // 从缓存获取作品详情
            List<WorkVO> workList = workIds.stream()
                    .map(id -> {
                        try {
                            return workService.getWorkDetail(Long.valueOf(id.toString()));
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            Page<WorkVO> page = new Page<>(pageNum, pageSize);
            page.setRecords(workList);
            page.setTotal(redisUtil.zCard(cacheKey));
            return page;
        }
        
        // 缓存未命中，从数据库查询
        return getFollowFeedFromDB(userId, pageNum, pageSize);
    }
    
    /**
     * 从数据库获取关注Feed
     */
    private Page<WorkVO> getFollowFeedFromDB(Long userId, int pageNum, int pageSize) {
        // 查询关注的用户ID列表
        LambdaQueryWrapper<Follow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getStatus, 0);
        
        List<Follow> follows = followMapper.selectList(followWrapper);
        List<Long> followedUserIds = follows.stream()
                .map(Follow::getFollowedUserId)
                .collect(Collectors.toList());
        
        if (followedUserIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        
        // 查询这些用户的作品
        Page<Work> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Work> workWrapper = new LambdaQueryWrapper<>();
        workWrapper.in(Work::getUserId, followedUserIds)
                .orderByDesc(Work::getCreateTime);
        
        Page<Work> workPage = workMapper.selectPage(page, workWrapper);
        
        // 转换为VO
        List<WorkVO> voList = workPage.getRecords().stream()
                .map(work -> {
                    try {
                        return workService.getWorkDetail(work.getId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Page<WorkVO> voPage = new Page<>(pageNum, pageSize, workPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 推送作品到粉丝的Feed流（发布作品时调用）
     */
    public void pushToFollowers(Long workId, Long authorId) {
        // 查询作者的粉丝
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowedUserId, authorId)
                .eq(Follow::getStatus, 0);
        
        List<Follow> followers = followMapper.selectList(wrapper);
        
        // 判断使用推模式还是拉模式
        if (followers.size() < PUSH_THRESHOLD) {
            // 推模式：直接写入粉丝的收件箱
            for (Follow follow : followers) {
                String inboxKey = FEED_INBOX_KEY + follow.getUserId();
                redisUtil.zAdd(inboxKey, workId, System.currentTimeMillis());
                
                // 限制收件箱大小（保留最近1000条）
                redisUtil.zRemoveRange(inboxKey, 0, -1001);
            }
            log.info("推模式：作品 {} 推送给 {} 个粉丝", workId, followers.size());
        } else {
            // 拉模式：写入大V动态池
            String vipTimelineKey = "feed:vip_timeline:" + authorId;
            redisUtil.zAdd(vipTimelineKey, workId, System.currentTimeMillis());
            log.info("拉模式：作品 {} 写入大V动态池", workId);
        }
    }
    
    /**
     * 获取推荐Feed（热门作品）
     */
    public Page<WorkVO> getRecommendFeed(int pageNum, int pageSize) {
        Page<Work> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Work::getHotScore);
        
        Page<Work> workPage = workMapper.selectPage(page, wrapper);
        
        List<WorkVO> voList = workPage.getRecords().stream()
                .map(work -> {
                    try {
                        return workService.getWorkDetail(work.getId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Page<WorkVO> voPage = new Page<>(pageNum, pageSize, workPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
}
