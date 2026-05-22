package com.liluo.moyan.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.framework.common.ErrorCode;
import com.liluo.moyan.modules.auth.dto.LoginRequest;
import com.liluo.moyan.modules.auth.dto.RegisterRequest;
import com.liluo.moyan.modules.user.dto.UpdateProfileRequest;
import com.liluo.moyan.infrastructure.user.entity.User;
import com.liluo.moyan.framework.exception.BusinessException;
import com.liluo.moyan.infrastructure.user.mapper.UserMapper;
import com.liluo.moyan.framework.security.JwtUtil;
import com.liluo.moyan.framework.util.RedisUtil;
import com.liluo.moyan.infrastructure.user.vo.TokenVO;
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
     * 刷新 Token（带防重放机制）
     */
    public TokenVO refreshToken(String refreshToken) {
        // 1. 验证 Refresh Token
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String jti = jwtUtil.getJtiFromToken(refreshToken);
        
        // 2. 检查 Redis 白名单
        String refreshKey = "refresh:" + userId + ":" + jti;
        if (!redisUtil.hasKey(refreshKey)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        
        // 3. 防重放检测（原子操作 SETNX）
        String usedKey = "used:" + jti;
        Boolean isFirst = redisUtil.setIfAbsent(usedKey, "1", 60);  // 60秒过期
        if (!isFirst) {
            // 检测到重放攻击，吊销该用户所有会话
            log.warn("检测到重放攻击: userId={}, jti={}", userId, jti);
            revokeAllUserTokens(userId);
            throw new BusinessException("检测到重放攻击，请重新登录");
        }
        
        // 4. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 5. 删除旧的 refresh token
        redisUtil.delete(refreshKey);
        
        // 6. 生成新的 tokens
        return generateTokens(user);
    }
    
    /**
     * 吊销用户所有会话
     */
    private void revokeAllUserTokens(Long userId) {
        // 删除该用户所有的 refresh token
        String pattern = "refresh:" + userId + ":*";
        java.util.Set<String> keys = redisUtil.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            // 逐个删除
            for (String key : keys) {
                redisUtil.delete(key);
            }
        }
        log.info("已吊销用户 {} 的所有会话", userId);
    }
    
    /**
     * 注销（将 access token 加入黑名单）
     */
    public void logout(String accessToken) {
        String jti = jwtUtil.getJtiFromToken(accessToken);
        long expiration = jwtUtil.parseToken(accessToken).getExpiration().getTime() - System.currentTimeMillis();

        // 加入黑名单，过期时间为 token 剩余有效期
        redisUtil.set("blacklist:" + jti, "1", expiration, TimeUnit.MILLISECONDS);

        // 删除 refresh token
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        String redisKey = "refresh:" + userId + ":" + jti;
        redisUtil.delete(redisKey);
    }
    
    /**
     * 生成双令牌
     */
    private TokenVO generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());
        
        // 存储 refresh token 到 Redis
        String jti = jwtUtil.getJtiFromToken(refreshToken);
        String redisKey = "refresh:" + user.getId() + ":" + jti;
        redisUtil.set(redisKey, "1", 7, TimeUnit.DAYS);
        
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
