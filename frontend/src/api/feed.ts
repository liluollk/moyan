import request from '../utils/request';
import type { Result, WorkVO, PageResult } from '../types';

export const getFollowFeed = (pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>('/feed/follow', {
    params: { pageNum, pageSize }
  });
};

export const getRecommendFeed = (pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>('/feed/recommend', {
    params: { pageNum, pageSize }
  });
};
