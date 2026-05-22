package com.liluo.moyan.framework.exception;

import com.liluo.moyan.framework.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        log.error("参数校验异常: {}", e.getMessage());
        return Result.error(400, "参数校验失败");
    }
    
    /**
     * 安全异常
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public Result<?> handleAuthenticationException(Exception e) {
        log.error("认证异常: {}", e.getMessage());
        return Result.error(401, "认证失败");
    }
    
    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统错误，请稍后重试");
    }
}
