import request from '../utils/request';
import type { Result, WorkVO, PageResult } from '../types';

// 获取关注Feed
export const getFollowFeed = (pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>('/feed/follow', {
    params: { pageNum, pageSize }
  });
};

// 获取推荐Feed
export const getRecommendFeed = (pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>('/feed/recommend', {
    params: { pageNum, pageSize }
  });
};
