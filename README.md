# 墨言创意社区 (Moyan)

一个面向创作者的分享平台，支持作品发布、图片上传、点赞收藏、评论互动、关注动态、全文搜索、AI智能回答等功能。

## 技术选型

| 层级 | 技术                                          |
|------|---------------------------------------------|
| 后端框架 | Spring Boot 3.5 + Java 21                   |
| 持久层 | MyBatis-Plus 3.5 + MySQL 8.0                |
| 缓存 | Redis (Redisson) + Caffeine 本地缓存            |
| 消息队列 | RabbitMQ (异步通知、ES 索引同步)                     |
| 搜索引擎 | Elasticsearch 9.x + IK 分词器                  |
| 安全认证 | Spring Security + JWT 双 Token               |
| 接口文档 | SpringDoc OpenAPI (Swagger)                 |
| 文件存储 | 本地存储 / 阿里云 OSS 可切换                          |
| 前端 | React 18 + TypeScript + Vite + Ant Design 5 |

## 功能

- 用户系统：手机号注册登录、JWT Token 无感刷新、个人资料编辑
- 作品管理：Markdown 发布、多图上传、编辑/删除
- 社交互动：点赞、收藏、评论、关注/取关
- 信息流：推荐 Feed + 关注动态 Feed (Redis ZSet 推送)
- 全文搜索：IK 中文分词、多字段检索、关键词高亮
- 排行榜：综合热度排序 (点赞×3 + 收藏×2 + 评论)
- 实时通知：点赞/收藏/评论/关注事件通知
- AI 智能问答：基于社区作品内容的 RAG 问答，流式输出，支持多轮对话

## 快速开始

### 环境要求

JDK 21、MySQL 8.0+、Redis 6.0+、RabbitMQ 3.x+、Elasticsearch 9.x+ (需 IK 分词器)、Node.js 18+

### 1. 初始化数据库

```sql
SOURCE src/main/resources/db/schema.sql;
SOURCE src/main/resources/db/test_data.sql;
```

### 2. 配置应用

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 编辑 application.yml，填入数据库密码和中间件地址
```

### 3. 启动后端

```bash
mvn spring-boot:run
# 启动在 http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
# 启动在 http://localhost:3000
```

### 测试账号

| 手机号 | 密码 |
|--------|------|
| 13800138000 | 123456 |
| 13800138001 | 123456 |
| 13800138002 | 123456 |

## 项目结构

```
moyan/
├── src/main/java/com/liluo/moyan/
│   ├── module/                 # 业务模块 (auth/user/work/follow/like/favorite/comment/feed/search/notification/file/ai)
│   ├── common/                 # 框架层 (config/interceptor/exception/util/result/aspect)
│   └── resources/
│       ├── db/                 # 建表脚本 + 测试数据
│       └── application-example.yml
└── frontend/src/
    ├── pages/                  # 页面组件 (auth/home/work/search/rank/user/notification/ask)
    ├── components/             # 通用组件 (WorkCard, MainLayout)
    ├── api/                    # API 封装
    ├── types/                  # TypeScript 类型
    └── utils/                  # 工具 (axios 封装 + Token 刷新)
```

## API 概览

```
POST   /api/auth/login|register        # 登录/注册
POST   /api/auth/refresh               # 刷新 Token
GET    /api/auth/me                    # 当前用户信息
PUT    /api/auth/profile               # 更新资料

GET    /api/works                      # 作品列表
GET    /api/works/{id}                 # 作品详情
POST   /api/works                      # 发布作品
PUT    /api/works                      # 编辑作品
DELETE /api/works/{id}                 # 删除作品
GET    /api/works/ranking              # 排行榜

POST   /api/likes/{workId}             # 点赞
DELETE /api/likes/{workId}             # 取消点赞

POST   /api/favorites/{workId}         # 收藏
DELETE /api/favorites/{workId}         # 取消收藏
GET    /api/favorites                  # 我的收藏

POST   /api/follows/{userId}           # 关注
DELETE /api/follows/{userId}           # 取消关注
GET    /api/follows/followers/{id}     # 粉丝列表
GET    /api/follows/following/{id}     # 关注列表

POST   /api/comments                   # 发评论
GET    /api/comments/work/{workId}     # 作品评论

GET    /api/search/works               # 搜索

POST   /api/files/upload               # 上传文件

GET    /api/notifications              # 通知列表
GET    /api/notifications/unread-count # 未读数

POST   /api/ai/ask                    # AI 问答 (SSE 流式)

GET    /api/feed/follow                # 关注动态
GET    /api/feed/recommend             # 推荐流
```
