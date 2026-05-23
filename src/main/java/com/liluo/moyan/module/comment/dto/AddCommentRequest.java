package com.liluo.moyan.module.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 添加评论请求 DTO
 */
@Data
public class AddCommentRequest {

    @NotNull(message = "作品ID不能为空")
    private Long workId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;
}
