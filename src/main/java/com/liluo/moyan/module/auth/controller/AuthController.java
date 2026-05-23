package com.liluo.moyan.module.auth.controller;

import com.liluo.moyan.common.result.ErrorCode;
import com.liluo.moyan.common.result.Result;
import com.liluo.moyan.module.auth.dto.LoginRequest;
import com.liluo.moyan.module.auth.dto.RegisterRequest;
import com.liluo.moyan.module.user.entity.User;
import com.liluo.moyan.module.auth.service.AuthService;
import com.liluo.moyan.common.interceptor.UserHolder;
import com.liluo.moyan.module.user.vo.TokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证模块", description = "用户注册、登录、刷新Token等")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<Void> sendVerifyCode(@RequestParam String phone) {
        authService.sendVerifyCode(phone);
        return Result.success();
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<TokenVO> register(@Valid @RequestBody RegisterRequest request) {
        TokenVO tokenVO = authService.register(request);
        return Result.success(tokenVO);
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginRequest request) {
        TokenVO tokenVO = authService.login(request);
        return Result.success(tokenVO);
    }
    
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<TokenVO> refreshToken(@RequestParam String refreshToken) {
        TokenVO tokenVO = authService.refreshToken(refreshToken);
        return Result.success(tokenVO);
    }
    
    @Operation(summary = "注销")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        authService.logout(token);
        return Result.success();
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<User> getCurrentUserInfo() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }
        User user = authService.getCurrentUserInfo(userId);
        return Result.success(user);
    }
}
