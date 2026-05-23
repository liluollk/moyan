package com.liluo.moyan.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求 DTO
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @Size(max = 255, message = "头像URL不能超过255个字符")
    private String avatar;

    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String intro;
}
