import request from '../utils/request';
import type { Result } from '../types';

export const likeWork = (workId: string) => {
  return request.post<Result<void>>(`/likes/${workId}`);
};

export const unlikeWork = (workId: string) => {
  return request.delete<Result<void>>(`/likes/${workId}`);
};

export const getLikeCount = (workId: string) => {
  return request.get<Result<number>>(`/likes/${workId}/count`);
};

export const isLiked = (workId: string) => {
  return request.get<Result<boolean>>(`/likes/${workId}/status`);
};
