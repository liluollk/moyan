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
  id: number;
  phone: string;
  nickname: string;
  avatar: string;
  intro: string;
  createTime: string;
  updateTime: string;
}

// 作品信息
export interface WorkVO {
  id: number;
  userId: number;
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
  id: number;
  userId: number;
  type: number; // 1-点赞 2-收藏 3-评论 4-关注
  content: string;
  isRead: number; // 0-未读 1-已读
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
  id: number;
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

// 评论信息
export interface CommentVO {
  id: number;
  userId: number;
  nickname: string;
  avatar: string;
  workId: number;
  content: string;
  createTime: string;
}

// 添加评论请求
export interface AddCommentRequest {
  workId: number;
  content: string;
}
