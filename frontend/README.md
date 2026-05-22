# 墨言前端项目

基于 React + TypeScript + Ant Design 的前端应用，严格对照后端接口开发。

## 技术栈

- React 18
- TypeScript
- Vite
- Ant Design 5
- React Router 6
- Axios
- Day.js

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口层
│   │   ├── auth.ts       # 认证相关接口
│   │   ├── work.ts       # 作品相关接口
│   │   ├── feed.ts       # Feed流接口
│   │   ├── like.ts       # 点赞接口
│   │   ├── favorite.ts   # 收藏接口
│   │   ├── follow.ts     # 关注接口
│   │   ├── notification.ts # 通知接口
│   │   ├── search.ts     # 搜索接口
│   │   └── file.ts       # 文件上传接口
│   ├── components/       # 公共组件
│   │   ├── MainLayout.tsx # 主布局
│   │   └── WorkCard.tsx  # 作品卡片
│   ├── pages/            # 页面组件
│   │   ├── Login.tsx     # 登录页
│   │   ├── Register.tsx  # 注册页
│   │   ├── Home.tsx      # 首页
│   │   ├── FollowFeed.tsx # 关注动态
│   │   ├── RecommendFeed.tsx # 推荐动态
│   │   ├── Publish.tsx   # 发布作品
│   │   ├── WorkDetail.tsx # 作品详情
│   │   ├── Notifications.tsx # 通知中心
│   │   └── Search.tsx    # 搜索页
│   ├── types/            # TypeScript类型定义
│   │   └── index.ts
│   ├── utils/            # 工具函数
│   │   └── request.ts    # Axios封装
│   ├── App.tsx           # 应用入口
│   ├── main.tsx          # React入口
│   └── index.css         # 全局样式
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 功能模块

### 1. 认证模块
- 用户登录
- 用户注册（带验证码）
- Token自动刷新
- 退出登录

### 2. 作品模块
- 发布作品（支持多图上传）
- 查看作品列表
- 查看作品详情
- 更新/删除作品

### 3. Feed流模块
- 首页作品流
- 关注动态流
- 推荐作品流

### 4. 互动模块
- 点赞/取消点赞
- 收藏/取消收藏
- 关注/取消关注

### 5. 通知模块
- 实时未读通知数
- 通知列表（点赞、收藏、评论、关注）

### 6. 搜索模块
- 关键词搜索作品

### 7. 文件模块
- 图片上传（阿里云OSS）

## API接口对应关系

| 前端页面 | 后端Controller | 接口路径 |
|---------|---------------|---------|
| 登录/注册 | AuthController | /api/auth/* |
| 作品管理 | WorkController | /api/works/* |
| Feed流 | FeedController | /api/feed/* |
| 点赞 | LikeController | /api/likes/* |
| 收藏 | FavoriteController | /api/favorites/* |
| 关注 | FollowController | /api/follows/* |
| 通知 | NotificationController | /api/notifications/* |
| 搜索 | SearchController | /api/search/* |
| 文件上传 | FileController | /api/files/* |

## 注意事项

1. 确保后端服务运行在 http://localhost:8080
2. 首次使用需要注册账号
3. 图片上传需要配置阿里云OSS
4. Token过期会自动跳转到登录页

## 开发说明

所有前端代码严格对照后端接口开发，确保：
- 请求路径完全匹配
- 请求参数类型一致
- 响应数据结构对应
- 错误处理完善
