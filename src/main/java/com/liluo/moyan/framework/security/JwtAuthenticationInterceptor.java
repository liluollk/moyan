package com.liluo.moyan.framework.security;

import com.liluo.moyan.framework.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    public JwtAuthenticationInterceptor(JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 检查 Token 是否在黑名单中
            String jti = jwtUtil.getJtiFromToken(token);
            if (redisUtil.hasKey("blacklist:" + jti)) {
                log.warn("Token 已在黑名单中: {}", jti);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效\",\"data\":null}");
                return false;
            }
            
            try {
                if (jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    
                    // 设置用户ID到 ThreadLocal
                    UserHolder.setUserId(userId);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("用户 {} 认证成功", userId);
                    return true;
                }
            } catch (Exception e) {
                log.error("JWT 认证失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}");
                return false;
            }
        }
        
        // 没有 Token，放行（由 Spring Security 决定是否拒绝）
        return true;
    }
    
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        // 请求完成后清除 ThreadLocal，防止内存泄漏
        UserHolder.clear();
    }
}
