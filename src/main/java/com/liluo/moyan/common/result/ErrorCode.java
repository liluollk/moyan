package com.liluo.moyan.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    SUCCESS(200, "成功"),
    
    // 参数错误 400-499
    PARAM_ERROR(400, "参数错误"),
    PHONE_FORMAT_ERROR(401, "手机号格式错误"),
    VERIFY_CODE_ERROR(402, "验证码错误"),
    PASSWORD_ERROR(403, "密码错误"),
    
    // 认证错误 401
    UNAUTHORIZED(401, "未授权"),
    TOKEN_EXPIRED(4011, "Token已过期"),
    TOKEN_INVALID(4012, "Token无效"),
    
    // 权限错误 403
    FORBIDDEN(403, "禁止访问"),
    
    // 业务错误 500-599
    USER_NOT_FOUND(5001, "用户不存在"),
    USER_ALREADY_EXISTS(5002, "用户已存在"),
    WORK_NOT_FOUND(5003, "作品不存在"),
    FOLLOW_ERROR(5004, "关注操作失败"),
    
    // 系统错误
    SYSTEM_ERROR(500, "系统错误"),
    DATABASE_ERROR(501, "数据库错误"),
    REDIS_ERROR(502, "Redis错误"),
    MQ_ERROR(503, "消息队列错误");
    
    private final Integer code;
    private final String message;
}
