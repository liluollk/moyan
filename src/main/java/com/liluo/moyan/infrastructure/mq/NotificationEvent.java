package com.liluo.moyan.infrastructure.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 接收者ID（作品作者）
     */
    private Long receiverUserId;
    
    /**
     * 通知类型：LIKE, FAVORITE, FOLLOW, COMMENT
     */
    private String type;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联的作品ID或用户ID（便于跳转）
     */
    private Long sourceId;
    
    /**
     * 操作者ID（谁操作的，可选）
     */
    private Long operatorUserId;
}
