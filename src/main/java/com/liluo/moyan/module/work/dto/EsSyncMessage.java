package com.liluo.moyan.module.work.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * ES 同步消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsSyncMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 作品ID
     */
    private Long workId;
    
    /**
     * 操作类型：CREATE, UPDATE, DELETE
     */
    private String action;
}
