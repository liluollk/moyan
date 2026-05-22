package com.liluo.moyan.modules.work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布作品请求 DTO
 */
@Data
public class PublishWorkRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;
    
    private String content;
    
    private List<String> images;  // 图片URL列表
}
