// Long 类型 ID 在后端序列化为字符串，前端统一用 string 避免 JS 精度丢失
export type ID = string;

// 统一响应结果
export interface Result<T = any> {
  code: number;
  message: string;
  data: T;
}

// Token响应
export interface TokenVO {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// 用户信息
export interface User {
  id: ID;
  phone: string;
  nickname: string;
  avatar: string;
  intro: string;
  createTime: string;
  updateTime: string;
}

// 作品信息
export interface WorkVO {
  id: ID;
  userId: ID;
  nickname: string;
  avatar: string;
  title: string;
  content: string;
  images: string[];
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  isLiked: boolean;
  isFavorited: boolean;
  isFollowing: boolean;
  createTime: string;
}

// 分页结果
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 通知
export interface Notification {
  id: ID;
  userId: ID;
  type: number;
  content: string;
  isRead: number;
  createTime: string;
}

// 登录请求
export interface LoginRequest {
  phone: string;
  password: string;
}

// 注册请求
export interface RegisterRequest {
  phone: string;
  password: string;
  verifyCode: string;
}

// 发布作品请求
export interface PublishWorkRequest {
  title: string;
  content?: string;
  images?: string[];
}

// 更新作品请求
export interface UpdateWorkRequest {
  id: ID;
  title?: string;
  content?: string;
  images?: string[];
}

// 更新用户资料请求
export interface UpdateProfileRequest {
  nickname?: string;
  avatar?: string;
  intro?: string;
}

// 关注用户信息
export interface FollowUserVO {
  userId: ID;
  nickname: string;
  avatar: string;
  isFollowing: boolean;
}

// 评论信息
export interface CommentVO {
  id: ID;
  userId: ID;
  nickname: string;
  avatar: string;
  workId: ID;
  content: string;
  createTime: string;
}

// 添加评论请求
export interface AddCommentRequest {
  workId: ID;
  content: string;
}
