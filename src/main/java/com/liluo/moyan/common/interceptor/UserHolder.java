package com.liluo.moyan.common.interceptor;

/**
 * 用户上下文持有者
 * 使用 ThreadLocal 存储当前请求的用户信息
 */
public class UserHolder {
    
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }
    
    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未设置则返回 null
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }
    
    /**
     * 清除当前用户ID
     * 必须在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        userIdHolder.remove();
    }
}
