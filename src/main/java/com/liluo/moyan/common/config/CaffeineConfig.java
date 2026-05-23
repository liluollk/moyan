package com.liluo.moyan.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置
 * 
 * 多级缓存架构：
 * - L1: Caffeine 本地缓存（进程内，~1ms）
 * - L2: Redis 分布式缓存（共享，~5ms）
 */
@Configuration
public class CaffeineConfig {
    
    // 缓存过期时间（秒）
    private static final long CAFFEINE_CACHE_EXPIRE = 600; // Caffeine 10分钟
    
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 定义缓存名称
        cacheManager.setCacheNames(Arrays.asList("workCache", "userCache"));
        
        // 配置 Caffeine
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)  // 最大缓存1000个条目
                .expireAfterWrite(CAFFEINE_CACHE_EXPIRE, TimeUnit.SECONDS)  // 写入后10分钟过期
                .recordStats());  // 开启统计信息（命中率等）
        
        return cacheManager;
    }
}
