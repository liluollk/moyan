package com.liluo.moyan.module.favorite.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体
 */
@Data
@TableName("user_favorite")
public class UserFavorite {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private Long workId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
