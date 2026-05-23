package com.liluo.moyan.module.follow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注实体
 */
@Data
@TableName("follow")
public class Follow {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;  // 关注者
    
    private Long followedUserId;  // 被关注者
    
    private Integer status;  // 0-已关注 1-已取消
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
