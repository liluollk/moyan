package com.liluo.moyan.infrastructure.init;

import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.modules.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final WorkMapper workMapper;
    private final SearchService searchService;

    public DataInitializer(WorkMapper workMapper, SearchService searchService) {
        this.workMapper = workMapper;
        this.searchService = searchService;
    }

    @Override
    public void run(String... args) {
        if (workMapper.selectCount(null) > 0) {
            log.info("作品表已有数据，跳过初始化");
        } else {
            log.info("开始插入测试作品数据...");

            Work[] works = {
                createWork(1L, "Spring Boot 3 实战指南", "Spring Boot 3带来了许多新特性：Java 17+支持、GraalVM原生镜像、改进的HTTP接口", 128, 45, 23, 95.5),
                createWork(2L, "React Hooks 深度解析", "使用Hooks让组件更简洁：useState, useEffect, useContext等", 256, 89, 56, 180.3),
                createWork(3L, "Figma 设计系统搭建", "一个完整的设计系统包含：色彩规范、字体规范、组件库、图标库", 89, 34, 12, 67.8),
                createWork(4L, "MySQL 性能优化实战", "索引优化和查询优化，避免全表扫描", 342, 156, 78, 245.6),
                createWork(5L, "城市夜景摄影技巧", "拍摄城市夜景的要点：使用三脚架、长曝光拍摄、合理设置ISO", 567, 234, 98, 423.9),
                createWork(1L, "Docker 容器化部署", "Docker让部署变得简单：docker build, docker run", 198, 67, 34, 134.2),
                createWork(2L, "TypeScript 高级技巧", "高级类型技巧：泛型约束、条件类型、映射类型", 445, 178, 89, 312.5),
                createWork(3L, "色彩理论在设计中的应用", "配色方案：单色系、互补色、三角色", 156, 67, 28, 112.7),
                createWork(4L, "Redis 缓存架构设计", "缓存策略：Cache-Aside、Read-Through、Write-Through", 289, 123, 56, 201.8),
                createWork(5L, "旅行摄影构图技巧", "经典构图法则：三分法、引导线、对称构图、框架构图", 678, 289, 134, 498.3)
            };

            for (Work work : works) {
                workMapper.insert(work);
            }

            log.info("成功插入 {} 个测试作品", works.length);
        }

        syncWorksToES();
    }

    private void syncWorksToES() {
        try {
            List<Work> allWorks = workMapper.selectList(null);
            if (allWorks.isEmpty()) {
                log.info("没有作品数据需要同步到ES");
                return;
            }

            log.info("开始同步 {} 个作品到 Elasticsearch", allWorks.size());
            int success = 0;
            int fail = 0;

            for (Work work : allWorks) {
                try {
                    searchService.indexWork(work);
                    success++;
                } catch (Exception e) {
                    fail++;
                    log.error("作品 {} ES索引失败: {}", work.getId(), e.getMessage());
                }
            }

            log.info("ES同步完成，成功: {}, 失败: {}", success, fail);
        } catch (Exception e) {
            log.error("ES同步过程出错", e);
        }
    }

    private Work createWork(Long userId, String title, String content,
                            int likeCount, int favoriteCount, int commentCount, double hotScore) {
        Work work = new Work();
        work.setUserId(userId);
        work.setTitle(title);
        work.setContent(content);
        work.setImages("[\"https://picsum.photos/800/600?random=" + (int)(Math.random() * 1000) + "\"]");
        work.setLikeCount(likeCount);
        work.setFavoriteCount(favoriteCount);
        work.setCommentCount(commentCount);
        work.setHotScore(hotScore);
        work.setCreateTime(LocalDateTime.now().minusHours((long)(Math.random() * 72)));
        work.setUpdateTime(LocalDateTime.now());
        work.setDeleted(0);
        return work;
    }
}
