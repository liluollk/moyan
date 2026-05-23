package com.liluo.moyan.module.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.module.favorite.entity.UserFavorite;
import com.liluo.moyan.module.work.entity.Work;
import com.liluo.moyan.common.exception.BusinessException;
import com.liluo.moyan.module.favorite.mapper.UserFavoriteMapper;
import com.liluo.moyan.module.work.mapper.WorkMapper;
import com.liluo.moyan.module.work.service.WorkService;
import com.liluo.moyan.module.work.vo.WorkVO;
import com.liluo.moyan.common.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private WorkService workService;
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

    /**
     * 获取当前用户收藏的作品列表
     */
    public List<WorkVO> getMyFavorites() {
        Long userId = UserHolder.getUserId();

        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime);
        List<UserFavorite> favorites = userFavoriteMapper.selectList(wrapper);

        return favorites.stream().map(fav -> {
            try {
                return workService.getWorkDetail(fav.getWorkId());
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
