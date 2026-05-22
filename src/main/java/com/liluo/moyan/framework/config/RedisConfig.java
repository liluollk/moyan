package com.liluo.moyan.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 适配 Spring Boot 3.x / Spring Data Redis 3.x
 */
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用 GenericJackson2JsonRedisSerializer（Spring Boot 3.x 推荐方式）
        // 无需手动配置 ObjectMapper，内部已优化
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // key 采用 String 的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // value 序列化方式采用 JSON
        template.setValueSerializer(jsonSerializer);
        // hash 的 value 序列化方式采用 JSON
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        return template;
    }
}
