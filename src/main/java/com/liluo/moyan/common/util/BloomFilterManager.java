package com.liluo.moyan.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liluo.moyan.module.work.entity.Work;
import com.liluo.moyan.module.work.mapper.WorkMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 布隆过滤器管理组件
 * 作用：拦截一定不存在的Key，防止缓存穿透
 * 特点：基于 Redisson 实现，支持分布式环境
 */
@Slf4j
@Component
public class BloomFilterManager {
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private WorkMapper workMapper;
    
    /**
     * 布隆过滤器名称
     */
    @Value("${bloom.filter.name:work-bloom-filter}")
    private String filterName;
    
    /**
     * 预期插入的数据总量
     */
    @Value("${bloom.filter.expected-insertions:100000}")
    private long expectedInsertions;
    
    /**
     * 期望的误判率（0.01 = 1%）
     */
    @Value("${bloom.filter.false-probability:0.01}")
    private double falseProbability;
    
    private RBloomFilter<String> bloomFilter;
    
    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void init() {
        bloomFilter = redissonClient.getBloomFilter(filterName);
        boolean initialized = bloomFilter.tryInit(expectedInsertions, falseProbability);

        if (initialized) {
            log.info("布隆过滤器 [{}] 初始化完成，预期容量={}, 误判率={}",
                    filterName, expectedInsertions, falseProbability);
        } else {
            log.info("布隆过滤器 [{}] 已存在，复用现有配置", filterName);
        }

        // 预热：从数据库加载所有作品ID到布隆过滤器
        warmUpFromDatabase();
    }
    
    /**
     * 从数据库预热布隆过滤器
     */
    private void warmUpFromDatabase() {
        try {
            LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(Work::getId);
            List<Work> works = workMapper.selectList(wrapper);
            
            if (works != null && !works.isEmpty()) {
                List<String> workIds = works.stream()
                        .map(work -> String.valueOf(work.getId()))
                        .toList();
                
                for (String id : workIds) {
                    bloomFilter.add(id);
                }
                
                log.info("布隆过滤器预热完成，共加载 {} 个作品ID", workIds.size());
            } else {
                log.info("数据库中没有作品数据，跳过布隆过滤器预热");
            }
        } catch (Exception e) {
            log.error("布隆过滤器预热失败", e);
        }
    }
    
    /**
     * 判断某个key是否一定不存在
     * 
     * @param key 待判断的key
     * @return true = 一定不存在，false = 可能存在
     */
    public boolean isNotExist(String key) {
        if (key == null || key.isEmpty()) {
            return true;
        }
        // contains返回true表示可能存在，false表示一定不存在
        return !bloomFilter.contains(key);
    }
    
    /**
     * 将key加入布隆过滤器
     * 
     * @param key 要添加的key
     */
    public void add(String key) {
        if (key != null && !key.isEmpty()) {
            bloomFilter.add(key);
            log.debug("布隆过滤器添加key: {}", key);
        }
    }
}
