package com.liluo.moyan.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
@TableName("notification")
public class Notification {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;  // 接收用户ID
    
    private Integer type;  // 1-点赞 2-收藏 3-评论 4-关注
    
    private String content;
    
    private Integer isRead;  // 0-未读 1-已读
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
