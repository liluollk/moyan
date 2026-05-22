package com.liluo.moyan.framework.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 操作日志切面（简单版）
 * 
 * 功能：记录 Service 层方法的执行时间和结果
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {
    
    /**
     * 切点：拦截所有 Service 层的方法
     */
    @Pointcut("execution(* com.liluo.moyan..service..*.*(..))")
    public void servicePointcut() {}
    
    /**
     * 环绕通知：记录方法执行时间
     */
    @Around("servicePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算耗时
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            
            // 记录成功日志
            log.info("{}.{} 执行成功，耗时: {} ms", className, methodName, executeTime);
            
            return result;
            
        } catch (Throwable e) {
            // 计算耗时
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            
            // 记录异常日志（包含完整堆栈信息）
            log.error("{}.{} 执行失败，耗时: {} ms", className, methodName, executeTime, e);
            
            throw e;
        }
    }
}
