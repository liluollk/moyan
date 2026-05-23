package com.liluo.moyan.module.like.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户点赞实体
 */
@Data
@TableName("user_like")
public class UserLike {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private Long workId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
