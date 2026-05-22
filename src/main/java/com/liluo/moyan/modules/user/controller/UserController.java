package com.liluo.moyan.modules.user.controller;

import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.user.dto.UpdateProfileRequest;
import com.liluo.moyan.infrastructure.user.entity.User;
import com.liluo.moyan.modules.auth.service.AuthService;
import com.liluo.moyan.framework.security.UserHolder;
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
