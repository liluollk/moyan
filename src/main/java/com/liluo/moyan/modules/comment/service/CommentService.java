package com.liluo.moyan.modules.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.framework.common.ErrorCode;
import com.liluo.moyan.infrastructure.mq.RabbitMQConfig;
import com.liluo.moyan.modules.comment.dto.AddCommentRequest;
import com.liluo.moyan.modules.comment.entity.Comment;
import com.liluo.moyan.infrastructure.user.entity.User;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.infrastructure.mq.NotificationEvent;
import com.liluo.moyan.framework.exception.BusinessException;
import com.liluo.moyan.modules.comment.mapper.CommentMapper;
import com.liluo.moyan.infrastructure.user.mapper.UserMapper;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.framework.util.RedisUtil;
import com.liluo.moyan.framework.security.UserHolder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import com.liluo.moyan.modules.comment.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CacheManager caffeineCacheManager;

    /**
     * 添加评论
     */
    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(AddCommentRequest request) {
        Long userId = UserHolder.getUserId();

        // 检查作品是否存在
        Work work = workMapper.selectById(request.getWorkId());
        if (work == null) {
            throw new BusinessException(ErrorCode.WORK_NOT_FOUND);
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setWorkId(request.getWorkId());
        comment.setContent(request.getContent());
        commentMapper.insert(comment);

        // 清除作品缓存（先清除缓存再更新数据库）
        invalidateWorkCache(request.getWorkId());

        // 更新作品评论数
        work.setCommentCount(work.getCommentCount() + 1);
        workMapper.updateById(work);

        log.info("用户 {} 评论作品 {}", userId, request.getWorkId());

        // 发送通知
        if (!work.getUserId().equals(userId)) {
            try {
                User user = userMapper.selectById(userId);
                String nickname = user != null ? user.getNickname() : "用户" + userId;

                NotificationEvent event = new NotificationEvent();
                event.setReceiverUserId(work.getUserId());
                event.setType("COMMENT");
                event.setContent(nickname + " 评论了你的作品《" + work.getTitle() + "》");
                event.setSourceId(work.getId());
                event.setOperatorUserId(userId);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_EXCHANGE,
                        RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                        event
                );
            } catch (Exception e) {
                log.error("发送评论通知失败", e);
            }
        }

        // 返回评论VO
        User user = userMapper.selectById(userId);
        return CommentVO.builder()
                .id(comment.getId())
                .userId(userId)
                .nickname(user != null ? user.getNickname() : "未知用户")
                .avatar(user != null ? user.getAvatar() : null)
                .workId(request.getWorkId())
                .content(request.getContent())
                .createTime(comment.getCreateTime())
                .build();
    }

    /**
     * 删除评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = UserHolder.getUserId();

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        commentMapper.deleteById(commentId);

        // 清除作品缓存
        invalidateWorkCache(comment.getWorkId());

        // 更新作品评论数
        Work work = workMapper.selectById(comment.getWorkId());
        if (work != null && work.getCommentCount() > 0) {
            work.setCommentCount(work.getCommentCount() - 1);
            workMapper.updateById(work);
        }

        log.info("用户 {} 删除评论 {}", userId, commentId);
    }

    /**
     * 获取作品评论列表
     */
    public Page<CommentVO> getComments(Long workId, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getWorkId, workId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        // 转换为VO
        List<CommentVO> voList = commentPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize, commentPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 清除作品缓存
     */
    private void invalidateWorkCache(Long workId) {
        String cacheKey = "work:" + workId;

        // 清除 Redis 缓存
        redisUtil.delete(cacheKey);

        // 清除 Caffeine 缓存
        Cache caffeineCache = caffeineCacheManager.getCache("workCache");
        if (caffeineCache != null) {
            caffeineCache.evict(cacheKey);
        }

        log.debug("作品缓存已清除: workId={}", workId);
    }

    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        User user = userMapper.selectById(comment.getUserId());

        return CommentVO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .nickname(user != null ? user.getNickname() : "未知用户")
                .avatar(user != null ? user.getAvatar() : null)
                .workId(comment.getWorkId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .build();
    }
}
