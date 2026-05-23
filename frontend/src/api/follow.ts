import request from '../utils/request';
import type { Result, FollowUserVO } from '../types';

// 关注用户
export const followUser = (userId: string) => {
  return request.post<Result<void>>(`/follows/${userId}`);
};

// 取消关注
export const unfollowUser = (userId: string) => {
  return request.delete<Result<void>>(`/follows/${userId}`);
};

// 检查是否关注
export const isFollowing = (userId: string) => {
  return request.get<Result<boolean>>(`/follows/check/${userId}`);
};

// 获取粉丝列表
export const getFollowers = (userId: string) => {
  return request.get<Result<FollowUserVO[]>>(`/follows/followers/${userId}`);
};

// 获取关注列表
export const getFollowing = (userId: string) => {
  return request.get<Result<FollowUserVO[]>>(`/follows/following/${userId}`);
};
