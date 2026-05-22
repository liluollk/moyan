package com.liluo.moyan.modules.work.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.framework.common.ErrorCode;
import com.liluo.moyan.infrastructure.mq.RabbitMQConfig;
import com.liluo.moyan.modules.work.dto.EsSyncMessage;
import com.liluo.moyan.modules.work.dto.PublishWorkRequest;
import com.liluo.moyan.modules.work.dto.UpdateWorkRequest;
import com.liluo.moyan.modules.follow.entity.Follow;
import com.liluo.moyan.infrastructure.user.entity.User;
import com.liluo.moyan.modules.favorite.entity.UserFavorite;
import com.liluo.moyan.modules.like.entity.UserLike;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.modules.feed.service.FeedService;
import com.liluo.moyan.framework.exception.BusinessException;
import com.liluo.moyan.modules.follow.mapper.FollowMapper;
import com.liluo.moyan.modules.favorite.mapper.UserFavoriteMapper;
import com.liluo.moyan.modules.like.mapper.UserLikeMapper;
import com.liluo.moyan.infrastructure.user.mapper.UserMapper;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.framework.util.BloomFilterManager;
import com.liluo.moyan.framework.util.RedisUtil;
import com.liluo.moyan.framework.security.UserHolder;
import com.liluo.moyan.modules.work.vo.WorkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 作品服务
 */
@Slf4j
@Service
public class WorkService {
    
    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLikeMapper userLikeMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private BloomFilterManager bloomFilterManager;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private FeedService feedService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CacheManager caffeineCacheManager;
    
    // 缓存键前缀
    private static final String WORK_CACHE_KEY = "work:";
    private static final String WORK_MUTEX_KEY = "mutex:work:";
    
    // 缓存过期时间（秒）
    private static final long REDIS_CACHE_EXPIRE = 1800; // 30分钟
    private static final long MUTEX_LOCK_EXPIRE = 10; // 互斥锁10秒
    private static final long CAFFEINE_CACHE_EXPIRE = 600; // Caffeine 10分钟
    /**
     * 发布作品
     */
    @Transactional(rollbackFor = Exception.class)
    public Long publishWork(PublishWorkRequest request) {
        Long userId = UserHolder.getUserId();
        
        // 创建作品
        Work work = new Work();
        work.setUserId(userId);
        work.setTitle(request.getTitle());
        work.setContent(request.getContent());
        
        // 转换图片列表为 JSON
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            work.setImages(JSONUtil.toJsonStr(request.getImages()));
        }
        
        work.setLikeCount(0);
        work.setFavoriteCount(0);
        work.setCommentCount(0);
        work.setHotScore(0.0);
        
        workMapper.insert(work);
        
        log.info("用户 {} 发布作品 {}", userId, work.getId());
        
        // 同步更新布隆过滤器
        bloomFilterManager.add(String.valueOf(work.getId()));
        
        // 预热缓存（可选，提升首次访问性能）
        try {
            WorkVO workVO = convertToVO(work);
            String cacheKey = WORK_CACHE_KEY + work.getId();
            
            // 写入Redis
            long randomOffset = ThreadLocalRandom.current().nextLong(300, 600);
            redisUtil.set(cacheKey, JSONUtil.toJsonStr(workVO), 
                REDIS_CACHE_EXPIRE + randomOffset, TimeUnit.SECONDS);
            
            // 写入Caffeine
            Cache caffeineCache = caffeineCacheManager.getCache("workCache");
            if (caffeineCache != null) {
                caffeineCache.put(cacheKey, workVO);
            }
            
            log.info("作品缓存预热成功: workId={}", work.getId());
        } catch (Exception e) {
            log.warn("作品缓存预热失败，不影响主流程: workId={}", work.getId(), e);
        }
        
        // 推送到粉丝Feed流
        try {
            feedService.pushToFollowers(work.getId(), userId);
        } catch (Exception e) {
            log.error("推送Feed流失败", e);
        }
        
        // 发送 MQ 消息异步同步到 ES（保证最终一致性）
        try {
            EsSyncMessage esMessage = new EsSyncMessage(work.getId(), "CREATE");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ES_SYNC_EXCHANGE,
                RabbitMQConfig.ES_SYNC_ROUTING_KEY,
                esMessage
            );
            log.info("发送ES同步消息: workId={}", work.getId());
        } catch (Exception e) {
            log.error("发送ES同步消息失败", e);
        }
        
        return work.getId();
    }
    
    /**
     * 获取作品详情（多级缓存 + 互斥锁防击穿）
     * 
     * 缓存架构：
     * 1. 布隆过滤器：拦截不存在的数据，防止缓存穿透
     * 2. Caffeine本地缓存：L1缓存，极速响应（~1ms）
     * 3. Redis分布式缓存：L2缓存，共享数据（~5ms）
     * 4. 数据库：最终数据源
     * 
     * 防护机制：
     * - 互斥锁：防止缓存击穿（热点key过期时大量请求同时查库）
     * - TTL随机偏移：防止缓存雪崩（大量key同时过期）
     */
    public WorkVO getWorkDetail(Long workId) {
        String cacheKey = WORK_CACHE_KEY + workId;

        // 1. 布隆过滤器拦截（防止缓存穿透）
        if (bloomFilterManager.isNotExist(String.valueOf(workId))) {
            log.debug("布隆过滤器拦截不存在的作品: {}", workId);
            throw new BusinessException(ErrorCode.WORK_NOT_FOUND);
        }

        // 2. 第一级缓存：Caffeine本地缓存
        Cache caffeineCache = caffeineCacheManager.getCache("workCache");
        if (caffeineCache != null) {
            WorkVO cached = caffeineCache.get(cacheKey, WorkVO.class);
            if (cached != null) {
                log.debug("Caffeine缓存命中: workId={}", workId);
                return fillUserStatus(cached);
            }
        }

        // 3. 第二级缓存：Redis
        String redisValue = (String) redisUtil.get(cacheKey);
        if (redisValue != null) {
            WorkVO workVO = JSONUtil.toBean(redisValue, WorkVO.class);
            // 回填Caffeine缓存
            if (caffeineCache != null) {
                caffeineCache.put(cacheKey, workVO);
            }
            log.debug("Redis缓存命中: workId={}", workId);
            return fillUserStatus(workVO);
        }

        // 4. 缓存未命中，使用互斥锁加载数据（防止缓存击穿）
        WorkVO workVO = loadWithMutexLock(cacheKey, workId, caffeineCache);
        return fillUserStatus(workVO);
    }
    
    /**
     * 使用互斥锁加载数据（防止缓存击穿）
     * 
     * 原理：
     * - 只有一个线程能获取到锁去查数据库
     * - 其他线程等待后重试，从缓存获取数据
     * - 避免大量请求同时打到数据库
     */
    private WorkVO loadWithMutexLock(String cacheKey, Long workId, Cache caffeineCache) {
        String mutexKey = WORK_MUTEX_KEY + workId;
        
        // 尝试获取分布式锁（SETNX）
        Boolean lock = redisUtil.setIfAbsent(mutexKey, "1", MUTEX_LOCK_EXPIRE);
        
        if (Boolean.TRUE.equals(lock)) {
            try {
                // 双重检查：可能其他线程已经加载完成
                String redisValue = (String) redisUtil.get(cacheKey);
                if (redisValue != null) {
                    WorkVO workVO = JSONUtil.toBean(redisValue, WorkVO.class);
                    if (caffeineCache != null) {
                        caffeineCache.put(cacheKey, workVO);
                    }
                    return workVO;
                }
                
                // 查询数据库
                log.info("从数据库加载作品: workId={}", workId);
                Work work = workMapper.selectById(workId);
                if (work == null) {
                    throw new BusinessException(ErrorCode.WORK_NOT_FOUND);
                }
                
                // 转换为VO
                WorkVO workVO = convertToVO(work);
                
                // 写入Redis缓存（TTL随机偏移，防止缓存雪崩）
                long randomOffset = ThreadLocalRandom.current().nextLong(300, 600); // 5-10分钟随机
                long expireTime = REDIS_CACHE_EXPIRE + randomOffset;
                redisUtil.set(cacheKey, JSONUtil.toJsonStr(workVO), expireTime, TimeUnit.SECONDS);
                
                // 回填Caffeine缓存
                if (caffeineCache != null) {
                    caffeineCache.put(cacheKey, workVO);
                }
                
                log.debug("作品缓存成功: workId={}, TTL={}s", workId, expireTime);
                return workVO;
                
            } finally {
                // 释放锁
                redisUtil.delete(mutexKey);
            }
        } else {
            // 未获取到锁，短暂等待后重试（递归调用）
            try {
                Thread.sleep(50); // 等待50ms
                return getWorkDetail(workId); // 重试，从缓存获取
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待锁被中断: workId={}", workId);
                throw new BusinessException("获取作品失败");
            }
        }
    }

    /**
     * 填充当前用户的点赞/收藏/关注状态
     * 这是用户特定的状态，不能缓存
     */
    private WorkVO fillUserStatus(WorkVO workVO) {
        Long currentUserId = UserHolder.getUserId();
        if (currentUserId == null) {
            workVO.setIsLiked(false);
            workVO.setIsFavorited(false);
            workVO.setIsFollowing(false);
            return workVO;
        }

        LambdaQueryWrapper<UserLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserLike::getUserId, currentUserId)
                   .eq(UserLike::getWorkId, workVO.getId());
        workVO.setIsLiked(userLikeMapper.selectCount(likeWrapper) > 0);

        LambdaQueryWrapper<UserFavorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(UserFavorite::getUserId, currentUserId)
                       .eq(UserFavorite::getWorkId, workVO.getId());
        workVO.setIsFavorited(userFavoriteMapper.selectCount(favoriteWrapper) > 0);

        // 查询是否关注作品作者
        if (!currentUserId.equals(workVO.getUserId())) {
            LambdaQueryWrapper<Follow> followWrapper = new LambdaQueryWrapper<>();
            followWrapper.eq(Follow::getUserId, currentUserId)
                         .eq(Follow::getFollowedUserId, workVO.getUserId())
                         .eq(Follow::getStatus, 0);
            workVO.setIsFollowing(followMapper.selectCount(followWrapper) > 0);
        } else {
            workVO.setIsFollowing(false); // 自己的作品，不需要关注
        }

        return workVO;
    }

    /**
     * 更新作品
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateWork(UpdateWorkRequest request) {
        Long userId = UserHolder.getUserId();
        
        // 查询作品
        Work work = workMapper.selectById(request.getId());
        if (work == null) {
            throw new BusinessException(ErrorCode.WORK_NOT_FOUND);
        }
        
        // 验证权限
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 更新
        work.setTitle(request.getTitle());
        work.setContent(request.getContent());
        workMapper.updateById(work);
        
        log.info("用户 {} 更新作品 {}", userId, work.getId());
        
        // 清除缓存（保证数据一致性）
        invalidateCache(work.getId());
        
        // 发送 MQ 消息异步更新 ES 索引
        try {
            EsSyncMessage esMessage = new EsSyncMessage(work.getId(), "UPDATE");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ES_SYNC_EXCHANGE,
                RabbitMQConfig.ES_SYNC_ROUTING_KEY,
                esMessage
            );
            log.info("发送ES更新消息: workId={}", work.getId());
        } catch (Exception e) {
            log.error("发送ES更新消息失败", e);
        }
    }
    
    /**
     * 删除作品
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWork(Long workId) {
        Long userId = UserHolder.getUserId();
        
        // 查询作品
        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException(ErrorCode.WORK_NOT_FOUND);
        }
        
        // 验证权限
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 逻辑删除
        workMapper.deleteById(workId);
        
        log.info("用户 {} 删除作品 {}", userId, workId);
        
        // 清除缓存
        invalidateCache(workId);
        
        // 发送 MQ 消息异步删除 ES 索引
        try {
            EsSyncMessage esMessage = new EsSyncMessage(workId, "DELETE");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ES_SYNC_EXCHANGE,
                RabbitMQConfig.ES_SYNC_ROUTING_KEY,
                esMessage
            );
            log.info("发送ES删除消息: workId={}", workId);
        } catch (Exception e) {
            log.error("发送ES删除消息失败", e);
        }
    }
    
    /**
     * 分页查询作品列表
     */
    public Page<WorkVO> getWorkList(int pageNum, int pageSize) {
        Page<Work> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Work::getCreateTime);
        
        Page<Work> workPage = workMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        List<WorkVO> voList = workPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<WorkVO> voPage = new Page<>(pageNum, pageSize, workPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 查询用户的作品列表
     */
    public Page<WorkVO> getUserWorks(Long userId, int pageNum, int pageSize) {
        Page<Work> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getUserId, userId)
                .orderByDesc(Work::getCreateTime);
        
        Page<Work> workPage = workMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        List<WorkVO> voList = workPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<WorkVO> voPage = new Page<>(pageNum, pageSize, workPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 转换为 VO
     */
    private WorkVO convertToVO(Work work) {
        // 查询作者信息
        User user = userMapper.selectById(work.getUserId());

        // 解析图片列表
        List<String> images = null;
        if (work.getImages() != null && !work.getImages().isEmpty()) {
            images = JSONUtil.toList(work.getImages(), String.class);
        }

        // 查询当前用户是否点赞/收藏/关注
        boolean isLiked = false;
        boolean isFavorited = false;
        boolean isFollowing = false;
        Long currentUserId = UserHolder.getUserId();
        if (currentUserId != null) {
            LambdaQueryWrapper<UserLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(UserLike::getUserId, currentUserId)
                       .eq(UserLike::getWorkId, work.getId());
            isLiked = userLikeMapper.selectCount(likeWrapper) > 0;

            LambdaQueryWrapper<UserFavorite> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(UserFavorite::getUserId, currentUserId)
                           .eq(UserFavorite::getWorkId, work.getId());
            isFavorited = userFavoriteMapper.selectCount(favoriteWrapper) > 0;

            // 查询是否关注作品作者
            if (!currentUserId.equals(work.getUserId())) {
                LambdaQueryWrapper<Follow> followWrapper = new LambdaQueryWrapper<>();
                followWrapper.eq(Follow::getUserId, currentUserId)
                             .eq(Follow::getFollowedUserId, work.getUserId())
                             .eq(Follow::getStatus, 0);
                isFollowing = followMapper.selectCount(followWrapper) > 0;
            }
        }

        return WorkVO.builder()
                .id(work.getId())
                .userId(work.getUserId())
                .nickname(user != null ? user.getNickname() : "未知用户")
                .avatar(user != null ? user.getAvatar() : null)
                .title(work.getTitle())
                .content(work.getContent())
                .images(images)
                .likeCount(work.getLikeCount())
                .favoriteCount(work.getFavoriteCount())
                .commentCount(work.getCommentCount())
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .isFollowing(isFollowing)
                .createTime(work.getCreateTime())
                .build();
    }
    
    /**
     * 清除作品缓存（更新/删除时调用）
     * 
     * @param workId 作品ID
     */
    private void invalidateCache(Long workId) {
        String cacheKey = WORK_CACHE_KEY + workId;
        
        // 清除 Redis 缓存
        redisUtil.delete(cacheKey);
        
        // 清除 Caffeine 缓存
        Cache caffeineCache = caffeineCacheManager.getCache("workCache");
        if (caffeineCache != null) {
            // Spring Cache的evict方法用于清除指定key
            caffeineCache.evict(cacheKey);
        }
        
        log.info("作品缓存已清除: workId={}", workId);
    }
}
