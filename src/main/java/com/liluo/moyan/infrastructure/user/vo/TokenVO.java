package com.liluo.moyan.infrastructure.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVO {
    
    private String accessToken;
    
    private String refreshToken;
    
    private Long expiresIn; // access token 过期时间（秒）
}
