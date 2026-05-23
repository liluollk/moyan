package com.liluo.moyan.module.user.controller;

import com.liluo.moyan.common.result.Result;
import com.liluo.moyan.module.user.dto.UpdateProfileRequest;
import com.liluo.moyan.module.auth.service.AuthService;
import com.liluo.moyan.common.interceptor.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        authService.updateUserProfile(userId, request);
        return Result.success();
    }
}
