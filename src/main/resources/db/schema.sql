-- ========================================
-- 墨言(Moyan) 数据库建表语句
-- 本地开发环境（单数据源）
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS moyan DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE moyan;

-- ========================================
-- 1. 用户表 (sys_user)
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID（雪花算法）',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    intro VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_phone (phone),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 作品表 (work)
-- ========================================
CREATE TABLE IF NOT EXISTS work (
    id BIGINT PRIMARY KEY COMMENT '作品ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    title VARCHAR(200) NOT NULL COMMENT '作品标题',
    content TEXT COMMENT '作品内容（Markdown或纯文本）',
    images TEXT COMMENT '图片URL数组（JSON字符串）',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    hot_score DOUBLE NOT NULL DEFAULT 0.0 COMMENT '热度分数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_hot_score (hot_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品表';

-- ========================================
-- 3. 用户点赞表 (user_like)
-- ========================================
CREATE TABLE IF NOT EXISTS user_like (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    work_id BIGINT NOT NULL COMMENT '作品ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_user_work (user_id, work_id),
    INDEX idx_work_id (work_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表';

-- ========================================
-- 4. 用户收藏表 (user_favorite)
-- ========================================
CREATE TABLE IF NOT EXISTS user_favorite (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    work_id BIGINT NOT NULL COMMENT '作品ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_work (user_id, work_id),
    INDEX idx_work_id (work_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ========================================
-- 5. 用户关注表 (follow)
-- ========================================
CREATE TABLE IF NOT EXISTS follow (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关注者ID',
    followed_user_id BIGINT NOT NULL COMMENT '被关注者ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-已关注 1-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_followed (user_id, followed_user_id),
    INDEX idx_followed_user_id (followed_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- ========================================
-- 6. 系统通知表 (notification)
-- ========================================
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type TINYINT NOT NULL COMMENT '通知类型 1-点赞 2-收藏 3-评论 4-关注',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ========================================
-- 7. 评论表 (comment)
-- ========================================
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY COMMENT '评论ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    work_id BIGINT NOT NULL COMMENT '作品ID',
    content VARCHAR(1000) NOT NULL COMMENT '评论内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_work_id (work_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
