package com.liluo.moyan.modules.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.modules.favorite.entity.UserFavorite;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.framework.exception.BusinessException;
import com.liluo.moyan.modules.favorite.mapper.UserFavoriteMapper;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.framework.security.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务
 */
@Slf4j
@Service
public class FavoriteService {
    
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    
    @Autowired
    private WorkMapper workMapper;
    /**
     * 收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void favoriteWork(Long workId) {
        Long userId = UserHolder.getUserId();
        
        // 检查是否已收藏
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getWorkId, workId);
        
        if (userFavoriteMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经收藏过");
        }
        
        // 添加收藏记录
        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setWorkId(workId);
        userFavoriteMapper.insert(userFavorite);
        
        // 更新作品收藏数
        workMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Work>()
                .eq(Work::getId, workId)
                .setSql("favorite_count = favorite_count + 1")
        );
        
        log.info("用户 {} 收藏作品 {}", userId, workId);
    }
    
    /**
     * 取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteWork(Long workId) {
        Long userId = UserHolder.getUserId();
        
        // 删除收藏记录
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getWorkId, workId);
        userFavoriteMapper.delete(wrapper);
        
        // 更新作品收藏数
        workMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Work>()
                .eq(Work::getId, workId)
                .setSql("favorite_count = GREATEST(favorite_count - 1, 0)")
        );
        
        log.info("用户 {} 取消收藏作品 {}", userId, workId);
    }
}
