package com.liluo.moyan.module.work.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品实体
 */
@Data
@TableName("work")
public class Work {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String images;  // JSON数组字符串
    
    private Integer likeCount;
    
    private Integer favoriteCount;
    
    private Integer commentCount;
    
    private Double hotScore;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
