import request from '../utils/request';
import type { Result } from '../types';

// 关注用户
export const followUser = (userId: number) => {
  return request.post<Result<void>>(`/follows/${userId}`);
};

// 取消关注
export const unfollowUser = (userId: number) => {
  return request.delete<Result<void>>(`/follows/${userId}`);
};

// 检查是否关注
export const isFollowing = (userId: number) => {
  return request.get<Result<boolean>>(`/follows/check/${userId}`);
};
