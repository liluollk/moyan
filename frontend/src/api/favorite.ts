import request from '../utils/request';
import type { Result } from '../types';

// 收藏作品
export const favoriteWork = (workId: number) => {
  return request.post<Result<void>>(`/favorites/${workId}`);
};

// 取消收藏
export const unfavoriteWork = (workId: number) => {
  return request.delete<Result<void>>(`/favorites/${workId}`);
};
