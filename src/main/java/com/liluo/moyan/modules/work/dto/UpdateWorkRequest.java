package com.liluo.moyan.modules.work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新作品请求 DTO
 */
@Data
public class UpdateWorkRequest {
    
    @NotNull(message = "作品ID不能为空")
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String content;
}
