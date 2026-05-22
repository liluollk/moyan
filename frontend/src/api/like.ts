import request from '../utils/request';
import type { Result } from '../types';

// 点赞作品
export const likeWork = (workId: number) => {
  return request.post<Result<void>>(`/likes/${workId}`);
};

// 取消点赞
export const unlikeWork = (workId: number) => {
  return request.delete<Result<void>>(`/likes/${workId}`);
};

// 查询作品点赞数
export const getLikeCount = (workId: number) => {
  return request.get<Result<number>>(`/likes/${workId}/count`);
};

// 查询当前用户是否已点赞
export const isLiked = (workId: number) => {
  return request.get<Result<boolean>>(`/likes/${workId}/status`);
};
