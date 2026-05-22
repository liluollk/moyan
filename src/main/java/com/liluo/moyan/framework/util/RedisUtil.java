package com.liluo.moyan.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Slf4j
@Component
public class RedisUtil {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 设置字符串值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 设置字符串值并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 获取字符串值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
    
    /**
     * 自增
     * 
     * @param key 键
     * @return 递增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    /**
     * 自减
     * 
     * @param key 键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
    
    /**
     * SETNX（原子操作，用于防重放）
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间（秒）
     * @return true=设置成功，false=已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 模糊查询 keys
     * 
     * @param pattern 模式（如 refresh:123:*）
     * @return key集合
     */
    public java.util.Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    /**
     * 添加到 Set
     * 
     * @param key 键
     * @param value 值
     * @return 是否添加成功（true=新元素，false=已存在）
     * 
     * 注意：底层 add() 返回 Long，这里转换为 boolean
     */
    public boolean addToSet(String key, Object value) {
        Long result = redisTemplate.opsForSet().add(key, value);
        return result != null && result > 0;
    }
    
    /**
     * 从 Set 移除
     * 
     * @param key 键
     * @param value 值
     * @return 移除的元素数量（0或1）
     */
    public Long removeFromSet(String key, Object value) {
        return redisTemplate.opsForSet().remove(key, value);
    }
    
    /**
     * 判断 Set 中是否包含
     */
    public Boolean isMemberOfSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }
    
    /**
     * 获取 Set 大小
     * 可用于统计在线用户数、点赞人数等
     * 
     * @param key 键
     * @return Set 元素数量
     */
    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }
    
    /**
     * 获取 Set 所有成员
     * 可用于查询所有点赞用户ID列表
     * 
     * @param key 键
     * @return Set 元素集合
     */
    public java.util.Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }
    
    /**
     * 添加到 ZSet
     */
    public Boolean addToZSet(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }
    
    /**
     * ZSet 添加（别名）
     * 
     * @param key 键
     * @param value 值
     * @param score 分数
     * @return 是否添加成功
     */
    public Boolean zAdd(String key, Object value, double score) {
        return addToZSet(key, value, score);
    }
    
    /**
     * 从 ZSet 移除
     * 可用于删除排行榜中的用户
     * 
     * @param key 键
     * @param value 值
     * @return 移除的元素数量
     */
    public Long removeFromZSet(String key, Object value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }
    
    /**
     * 获取 ZSet 大小
     */
    public Long getZSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
    
    /**
     * 按分数倒序获取 ZSet 范围
     */
    public java.util.Set<Object> reverseRangeZSet(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }
    
    /**
     * ZSet 倒序范围查询（别名）
     */
    public java.util.Set<Object> zReverseRange(String key, long start, long end) {
        return reverseRangeZSet(key, start, end);
    }
    
    /**
     * 按分数倒序获取 ZSet 范围（带分数）
     * 可用于获取排行榜及分数
     * 
     * @param key 键
     * @param start 起始位置
     * @param end 结束位置
     * @return 带分数的元素集合
     */
    public java.util.Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> 
    reverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }
    
    /**
     * 限制 ZSet 大小，移除多余的元素
     * 可用于维护热点数据缓存，只保留最近N条
     * 
     * @param key 键
     * @param maxSize 最大容量
     * @return 移除的元素数量
     */
    public Long trimZSet(String key, long maxSize) {
        long size = getZSetSize(key);
        if (size > maxSize) {
            return redisTemplate.opsForZSet().removeRange(key, 0, size - maxSize - 1);
        }
        return 0L;
    }
    
    /**
     * ZSet 移除范围（别名）
     * 
     * @param key 键
     * @param start 起始位置
     * @param end 结束位置
     * @return 移除的元素数量
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }
    
    /**
     * ZSet 大小（别名）
     */
    public Long zCard(String key) {
        return getZSetSize(key);
    }
}
