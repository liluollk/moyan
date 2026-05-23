package com.liluo.moyan.module.like.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 点赞/收藏事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 作品ID
     */
    private Long workId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 操作类型：LIKE（点赞）或 UNLIKE（取消点赞）
     */
    private String action;
}
