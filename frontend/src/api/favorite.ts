import request from '../utils/request';
import type { Result, WorkVO } from '../types';

export const favoriteWork = (workId: string) => {
  return request.post<Result<void>>(`/favorites/${workId}`);
};

export const unfavoriteWork = (workId: string) => {
  return request.delete<Result<void>>(`/favorites/${workId}`);
};

export const getMyFavorites = () => {
  return request.get<Result<WorkVO[]>>('/favorites');
};
