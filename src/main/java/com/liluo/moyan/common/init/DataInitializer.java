package com.liluo.moyan.common.init;

import com.liluo.moyan.module.work.entity.Work;
import com.liluo.moyan.module.work.mapper.WorkMapper;
import com.liluo.moyan.module.search.service.SearchService;
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
                createWork(1L, "Spring Boot 3核心特性", "基于Java 17，全面迁移到Jakarta EE命名空间；支持GraalVM原生镜像，实现毫秒级启动和极低内存占用；内置Java虚拟线程支持，提升高并发吞吐量；提供声明式HTTP客户端@HttpExchange和Problem Details标准化错误响应。最佳实践方面：使用Spring Initializr生成项目并合理配置多环境Profile；采用清晰分层架构，使用record定义不可变DTO；统一异常处理遵循RFC 7807规范；重点利用Actuator和Micrometer构建可观测性；集成Testcontainers进行真实集成测试；对于Native Image场景，需通过RuntimeHintsRegistrar处理反射和动态代理提示，确保AOT编译兼容。这些实践能帮助你构建云原生就绪的现代Java应用。", 328, 145, 67, 295.5),
                createWork(2L, "React Hooks的使用技巧", "首先遵循Hooks调用规则，只在顶层调用，不在循环、条件或嵌套函数中使用。使用useState时，若新状态依赖旧状态，传入函数形式；useEffect要明确依赖数组，避免遗漏或过多依赖，清理副作用返回函数。自定义Hook封装复用逻辑，以\"use\"开头。性能优化：useMemo缓存计算结果，useCallback缓存函数引用，避免子组件无效渲染。useReducer适合复杂状态逻辑；useContext配合useReducer实现轻量级状态管理。注意事项：避免在useEffect中直接修改状态引发无限循环；Hooks不能在普通函数中调用；严格模式下双重调用副作用需兼容；依赖数组中的引用类型可能导致意外更新，应拆解为基本类型或使用useRef存储可变值。", 456, 189, 123, 380.3),
                createWork(3L, "搭建Figma设计系统", "需从原子层逐级构建。底层是跨场景的设计变量，作为基础数据源；中层创建可配置变体的原子组件；上层组装成分子与模板。设计时要遵循可扩展性、性能及无障碍原则。必须建立清晰的命名规则与维护机制，并利用官方指南及案例等生态资源持续迭代进化。", 289, 89, 34, 167.8),
                createWork(4L, "数据库索引优化", "优先为WHERE、JOIN、ORDER BY后的高选择性列创建索引，避免索引过多拖慢写入。复合索引遵循最左前缀原则，把等值查询列放前面。尽量使用覆盖索引减少回表。避免在索引列上使用函数、隐式类型转换或LIKE '%xx'，否则索引失效。查询优化：用EXPLAIN分析执行计划，关注type（最好达到ref或range）、possible_keys和rows。拒绝SELECT *，只取必要字段。大表分页不要用OFFSET过大，改用游标或延迟关联。多表JOIN优先小表驱动大表，确保关联字段有索引。定期更新统计信息，避免参数嗅探。对于复杂查询，考虑拆分为多条简单查询或使用临时表。合理使用批量操作减少交互次数。这些实战经验能显著提升数据库响应速度。", 542, 256, 145, 445.6),
                createWork(5L, "城市夜景摄影技巧", "构图方面，利用低角度仰拍建筑展现压迫感，高机位俯拍车流形成光轨曲线。以前景（栏杆、水面倒影）增强层次，用引导线（街道、桥梁）延伸视线。遵循三分法，将亮部放在黄金分割点。后期处理：先调色温偏冷突出科技感，提高对比度与清晰度，降低高光保留灯光细节，提阴影恢复暗部纹理。用径向滤镜强化主体光源，线性渐变模拟光效。适当降低饱和度，用HSL统一色相（如让红色偏橙），增加噪点抑制。最后用蒙版锐化建筑边缘，避免整体锐化加重噪点。关键原则：保持画面干净，避免过曝。", 667, 289, 156, 523.9),
                createWork(1L, "搭建Figma设计系统核心流程", "第一步建立设计变量库，定义颜色（品牌色/功能色/中性色）、字体（字族/字重/行高）、间距（4px/8px倍数）、圆角、阴影等原子级数据。第二步创建组件集，使用变体（variant）制作多状态按钮（默认/悬浮/禁用/点击）、输入框、开关等基础组件，命名遵循\"类别/组件/属性/值\"规则（如Button/Primary/Size/Large）。第三步组装分子组件：搜索框（输入框+图标+按钮）、卡片（图片+标题+正文+操作区）。第四步利用自动布局（Auto Layout）实现自适应，设定约束和填充规则。第五步制作组件样式覆盖（可以通过嵌套实例覆盖文字或颜色，需提前定义可覆盖属性）。第六步通过团队库功能发布为库，团队成员可在其他文件引用并同步更新。注意规范：避免过多层级影响性能；使用网格对齐；对图标使用统一容器（24x24）并填充；提供文档说明使用场景和命名规则。定期清理未使用的组件样式。实践提示：先用小型项目试运行，再推广到全团队。", 398, 134, 78, 284.2),
                createWork(2L, "TypeScript高级技巧", "使用泛型约束（extends keyof）实现类型安全的对象属性访问。条件类型（T extends U ? X : Y）结合infer提取类型，如ReturnType。映射类型（{ [P in K]: T }）配合as重映射键名。模板字面量类型拼接字符串类型。变元组类型实现函数参数拼接。使用is关键字自定义类型守卫。利用never类型实现穷尽检查。重载函数精确声明不同参数返回值。递归类型处理深层对象。注意事项：避免过度抽象影响可读；分布式条件类型需包裹裸类型参数；函数重载实现要兼容所有签名；类型断言（as）谨慎使用；模块解析启用NodeNext支持ESM。使用@ts-expect-error代替@ts-ignore表明预期错误。利用品牌（brand）模拟名义类型防止结构类型误匹配。", 545, 234, 167, 412.5),
                createWork(3L, "色彩理论知识", "首先理解色彩三要素——色相（颜色种类）、饱和度（纯度）、明度（亮度）。掌握色轮关系：同类色（15°内）、邻近色（30-60°）、对比色（120°）、互补色（180°）。配色方法：单色搭配统一柔和；邻近色和谐舒适；互补色制造冲击；分裂互补平衡对比；三色搭配丰富活力。注意色彩情感效应：红色热情、蓝色冷静、绿色自然、黄色活力。实际设计时遵循60-30-10法则（主色60%、次要30%、点缀10%）。考虑无障碍设计，确保对比度满足WCAG标准（正常文本4.5:1）。使用HSL调色更直观。避免高饱和度大面积使用，善用中性色平衡。灰度模式先检查层次。定期提取优秀作品色板分析规律。", 256, 112, 45, 182.7),
                createWork(4L, "Redis缓存策略", "使用过期时间+LRU淘汰。预防缓存穿透（查询不存在数据）：布隆过滤器过滤非法key，或缓存空值（短时）。预防缓存雪崩（大量key同时失效）：设置随机过期时间偏移；热点数据永不过期+异步更新。预防缓存击穿（热点key失效高并发）：互斥锁（SETNX）只允许一个线程重建缓存。更新策略：先更新数据库，再删除缓存（Cache Aside）。使用管道批量操作减少RTT。监控命中率和内存。大key拆分，避免阻塞。", 489, 198, 89, 351.8),
                createWork(5L, "记录旅行瞬间", "关键在于留心观察与预判。清晨和黄昏的光线最柔和，提前到达选好机位。构图多用引导线（路、栏杆）或框架（窗、门）增加纵深感。拍摄人物时抓拍自然状态，避免僵硬摆拍。善用前景虚化突出主体，注意背景简洁。后期适当增强对比与饱和度，但不过度修饰。尝试不同视角——仰拍建筑挺拔，俯拍街道生动。每一帧都是独特故事，享受过程比设备更重要。", 578, 245, 112, 448.3)
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