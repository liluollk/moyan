import request from '../utils/request';
import type { Result, WorkVO } from '../types';

// 搜索作品
export const searchWorks = (keyword: string, from: number = 0, size: number = 10) => {
  return request.get<Result<WorkVO[]>>('/search/works', {
    params: { keyword, from, size }
  });
};
