package com.liluo.moyan.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.common.result.ErrorCode;
import com.liluo.moyan.module.auth.dto.LoginRequest;
import com.liluo.moyan.module.auth.dto.RegisterRequest;
import com.liluo.moyan.module.user.dto.UpdateProfileRequest;
import com.liluo.moyan.module.user.entity.User;
import com.liluo.moyan.common.exception.BusinessException;
import com.liluo.moyan.module.user.mapper.UserMapper;
import com.liluo.moyan.common.interceptor.JwtUtil;
import com.liluo.moyan.common.util.RedisUtil;
import com.liluo.moyan.module.user.vo.TokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    public AuthService(UserMapper userMapper, 
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    /**
     * 发送验证码（模拟）
     */
    public void sendVerifyCode(String phone) {
        // 模拟生成6位验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 存入 Redis，5分钟过期
        redisUtil.set("verify_code:" + phone, code, 5, TimeUnit.MINUTES);
        
        log.info("发送验证码到 {}: {}", phone, code);
    }
    
    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public TokenVO register(RegisterRequest request) {
        // 验证手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, request.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        
        // 验证验证码
        Object storedCode = redisUtil.get("verify_code:" + request.getPhone());
        if (storedCode == null || !storedCode.toString().equals(request.getVerifyCode())) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_ERROR);
        }
        
        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname("用户" + request.getPhone().substring(7));
        
        userMapper.insert(user);
        
        // 删除验证码
        redisUtil.delete("verify_code:" + request.getPhone());
        
        // 生成 Token
        return generateTokens(user);
    }
    
    /**
     * 用户登录
     */
    public TokenVO login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, request.getPhone());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        // 生成 Token
        return generateTokens(user);
    }
    
    /**
     * 刷新 Token（滑动过期机制）
     * 
     * 策略：
     * 1. Refresh Token 固定不变（不轮换）
     * 2. 每次刷新时重置 TTL 为 7 天（滑动窗口）
     * 3. 只要用户持续活跃，Token 永不过期
     */
    public TokenVO refreshToken(String refreshToken) {
        // 1. 验证 Refresh Token 有效性
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        // 2. 检查 Redis 中是否存在该用户的 Refresh Token
        String refreshKey = "refresh:" + userId;
        String storedToken = (String) redisUtil.get(refreshKey);
        
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        
        // 3. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 4. 滑动过期：重置 Refresh Token 的 TTL 为 7 天
        redisUtil.expire(refreshKey, 7, TimeUnit.DAYS);
        log.debug("用户 {} Refresh Token TTL 已重置", userId);
        
        // 5. 生成新的 Access Token（Refresh Token 不变）
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        
        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)  // 返回同一个 Refresh Token
                .expiresIn(900L)  // 15分钟
                .build();
    }
    

    
    /**
     * 退出登录
     */
    public void logout(String accessToken) {
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        
        // 1. 删除 Redis 中的 Refresh Token
        String refreshKey = "refresh:" + userId;
        redisUtil.delete(refreshKey);
        
        // 2. 可选：将 Access Token 加入黑名单（防止剩余时间内继续使用）
        String jti = jwtUtil.getJtiFromToken(accessToken);
        long expiration = jwtUtil.parseToken(accessToken).getExpiration().getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisUtil.set("blacklist:" + jti, "1", expiration, TimeUnit.MILLISECONDS);
        }
        
        log.info("用户 {} 已退出登录", userId);
    }
    
    /**
     * 生成双令牌
     */
    private TokenVO generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());
        
        // 存储 Refresh Token 到 Redis（Key 只包含 userId）
        String redisKey = "refresh:" + user.getId();
        redisUtil.set(redisKey, refreshToken, 7, TimeUnit.DAYS);
        
        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(900L) // 15分钟
                .build();
    }
    
    /**
     * 获取当前用户信息
     */
    public User getCurrentUserInfo(Long userId) {
        log.info("查询用户信息, userId={}", userId);
        User user = userMapper.selectById(userId);
        log.info("查询结果: {}", user);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 清除敏感信息
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getIntro() != null) {
            user.setIntro(request.getIntro());
        }

        userMapper.updateById(user);
        log.info("用户 {} 更新资料成功", userId);
    }
}
