package com.liluo.moyan.framework.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类（双令牌）
 */
@Slf4j
@Component
public class JwtUtil {
    
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    
    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String phone) {
        return generateToken(userId, phone, accessTokenExpiration, "access");
    }
    
    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId, String phone) {
        return generateToken(userId, phone, refreshTokenExpiration, "refresh");
    }
    
    /**
     * 生成 Token
     */
    private String generateToken(Long userId, String phone, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        String jti = UUID.randomUUID().toString();
        
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .claim("type", type)
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("JWT解析失败: {}", e.getMessage());
            throw new IllegalArgumentException("Token无效或已过期");
        }
    }
    
    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 从 Token 中获取 JTI
     */
    public String getJtiFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }
    
    /**
     * 从 Token 中获取类型
     */
    public String getTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }
    
    /**
     * 判断是否为 Access Token
     */
    public boolean isAccessToken(String token) {
        return "access".equals(getTypeFromToken(token));
    }
    
    /**
     * 判断是否为 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTypeFromToken(token));
    }
}
