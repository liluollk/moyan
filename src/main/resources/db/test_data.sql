-- ========================================
-- 墨言(Moyan) 测试数据
-- ========================================

USE moyan;

-- 清空现有测试数据（避免主键冲突）
DELETE FROM notification;
DELETE FROM follow;
DELETE FROM user_favorite;
DELETE FROM user_like;
DELETE FROM work;
DELETE FROM sys_user WHERE id <= 5;

-- ========================================
-- 1. 插入测试用户（密码都是 123456，BCrypt加密）
-- ========================================
INSERT INTO sys_user (id, phone, password, nickname, avatar, intro, create_time, update_time, deleted) VALUES
(1, '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户1', 'https://picsum.photos/200/200?random=1', '热爱分享的创作者', NOW(), NOW(), 0),
(2, '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '小明同学', 'https://picsum.photos/200/200?random=2', '前端开发工程师', NOW(), NOW(), 0),
(3, '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '设计大师', 'https://picsum.photos/200/200?random=3', 'UI/UX设计师', NOW(), NOW(), 0),
(4, '13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '后端达人', 'https://picsum.photos/200/200?random=4', 'Java后端开发', NOW(), NOW(), 0),
(5, '13800138004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '摄影爱好者', 'https://picsum.photos/200/200?random=5', '用镜头记录美好', NOW(), NOW(), 0);

-- ========================================
-- 2. 插入测试作品
-- ========================================
INSERT INTO work (id, user_id, title, content, images, like_count, favorite_count, comment_count, hot_score, create_time, update_time, deleted) VALUES
(1, 1, 'Spring Boot 3 实战指南', '# Spring Boot 3 实战\n\nSpring Boot 3带来了许多新特性：\n- 基于Java 17+\n- 支持GraalVM原生镜像\n- 改进的HTTP接口\n\n让我们开始学习吧！', '["https://picsum.photos/800/600?random=10","https://picsum.photos/800/600?random=11"]', 128, 45, 23, 95.5, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0),
(2, 2, 'React Hooks 深度解析', '# React Hooks\n\n使用Hooks让组件更简洁：\n\n```jsx\nconst [count, setCount] = useState(0);\n```\n\nHooks是React 16.8引入的新特性。', '["https://picsum.photos/800/600?random=12"]', 256, 89, 56, 180.3, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0),
(3, 3, 'Figma 设计系统搭建', '# 设计系统\n\n一个完整的设计系统包含：\n1. 色彩规范\n2. 字体规范\n3. 组件库\n4. 图标库\n\n好的设计系统能提升团队效率。', '["https://picsum.photos/800/600?random=13","https://picsum.photos/800/600?random=14","https://picsum.photos/800/600?random=15"]', 89, 34, 12, 67.8, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0),
(4, 4, 'MySQL 性能优化实战', '# MySQL优化\n\n## 索引优化\n- 合理选择索引类型\n- 避免索引失效\n\n## 查询优化\n- 使用EXPLAIN分析\n- 避免全表扫描', '["https://picsum.photos/800/600?random=16"]', 342, 156, 78, 245.6, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 5 HOUR, 0),
(5, 5, '城市夜景摄影技巧', '# 夜景摄影\n\n拍摄城市夜景的要点：\n- 使用三脚架\n- 长曝光拍摄\n- 合理设置ISO\n- 利用蓝调时刻\n\n拍出 stunning 的城市夜景！', '["https://picsum.photos/800/600?random=17","https://picsum.photos/800/600?random=18"]', 567, 234, 98, 423.9, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, 0),
(6, 1, 'Docker 容器化部署', '# Docker入门\n\nDocker让部署变得简单：\n\n```bash\ndocker build -t myapp .\ndocker run -p 8080:8080 myapp\n```\n\n容器化是现代开发的必备技能。', '["https://picsum.photos/800/600?random=19"]', 198, 67, 34, 134.2, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, 0),
(7, 2, 'TypeScript 高级技巧', '# TypeScript\n\n高级类型技巧：\n- 泛型约束\n- 条件类型\n- 映射类型\n\n让代码更安全、更优雅。', '["https://picsum.photos/800/600?random=20","https://picsum.photos/800/600?random=21"]', 445, 178, 89, 312.5, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE, 0),
(8, 3, '色彩理论在设计中的应用', '# 色彩理论\n\n配色方案：\n- 单色系\n- 互补色\n- 三角色\n\n掌握色彩让设计更有感染力。', '["https://picsum.photos/800/600?random=22","https://picsum.photos/800/600?random=23","https://picsum.photos/800/600?random=24"]', 156, 67, 28, 112.7, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0),
(9, 4, 'Redis 缓存架构设计', '# Redis缓存\n\n缓存策略：\n1. Cache-Aside\n2. Read-Through\n3. Write-Through\n\n合理设计缓存架构提升系统性能。', '["https://picsum.photos/800/600?random=25"]', 289, 123, 56, 201.8, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, 0),
(10, 5, '旅行摄影构图技巧', '# 摄影构图\n\n经典构图法则：\n- 三分法\n- 引导线\n- 对称构图\n- 框架构图\n\n好构图让照片更有故事感。', '["https://picsum.photos/800/600?random=26","https://picsum.photos/800/600?random=27"]', 678, 289, 134, 498.3, NOW() - INTERVAL 15 MINUTE, NOW() - INTERVAL 15 MINUTE, 0);

-- ========================================
-- 3. 插入测试点赞数据
-- ========================================
INSERT INTO user_like (id, user_id, work_id, create_time) VALUES
(1, 2, 1, NOW() - INTERVAL 1 DAY),
(2, 3, 1, NOW() - INTERVAL 1 DAY),
(3, 1, 2, NOW() - INTERVAL 12 HOUR),
(4, 4, 2, NOW() - INTERVAL 12 HOUR),
(5, 5, 2, NOW() - INTERVAL 12 HOUR),
(6, 1, 4, NOW() - INTERVAL 2 HOUR),
(7, 2, 5, NOW() - INTERVAL 1 HOUR),
(8, 3, 5, NOW() - INTERVAL 1 HOUR),
(9, 1, 7, NOW() - INTERVAL 20 MINUTE),
(10, 4, 10, NOW() - INTERVAL 10 MINUTE);

-- ========================================
-- 4. 插入测试收藏数据
-- ========================================
INSERT INTO user_favorite (id, user_id, work_id, create_time) VALUES
(1, 2, 1, NOW() - INTERVAL 1 DAY),
(2, 1, 4, NOW() - INTERVAL 2 HOUR),
(3, 3, 5, NOW() - INTERVAL 1 HOUR),
(4, 4, 7, NOW() - INTERVAL 20 MINUTE);

-- ========================================
-- 5. 插入测试关注数据
-- ========================================
INSERT INTO follow (id, user_id, followed_user_id, status, create_time, update_time) VALUES
(1, 2, 1, 0, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(2, 3, 1, 0, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(3, 4, 2, 0, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(4, 5, 3, 0, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(5, 1, 5, 0, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- ========================================
-- 6. 插入测试通知数据
-- ========================================
INSERT INTO notification (id, user_id, type, content, is_read, create_time) VALUES
(1, 1, 1, '小明同学 点赞了你的作品《Spring Boot 3 实战指南》', 0, NOW() - INTERVAL 1 DAY),
(2, 1, 1, '设计大师 点赞了你的作品《Spring Boot 3 实战指南》', 0, NOW() - INTERVAL 1 DAY),
(3, 2, 1, '测试用户1 点赞了你的作品《React Hooks 深度解析》', 1, NOW() - INTERVAL 12 HOUR),
(4, 1, 4, '小明同学 关注了你', 0, NOW() - INTERVAL 5 DAY),
(5, 2, 4, '后端达人 关注了你', 1, NOW() - INTERVAL 3 DAY);

-- ========================================
-- 完成！测试数据已导入
-- ========================================
SELECT '测试数据导入成功！' AS message;
SELECT COUNT(*) AS user_count FROM sys_user;
SELECT COUNT(*) AS work_count FROM work;
SELECT COUNT(*) AS like_count FROM user_like;
SELECT COUNT(*) AS favorite_count FROM user_favorite;
SELECT COUNT(*) AS follow_count FROM follow;
SELECT COUNT(*) AS notification_count FROM notification;
