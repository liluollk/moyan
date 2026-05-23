import request from '../utils/request';
import type { Result, WorkVO, PublishWorkRequest, UpdateWorkRequest, PageResult } from '../types';

// 发布作品
export const publishWork = (data: PublishWorkRequest) => {
  return request.post<Result<string>>('/works', data);
};

// 获取作品详情
export const getWorkDetail = (workId: string) => {
  return request.get<Result<WorkVO>>(`/works/${workId}`);
};

// 更新作品
export const updateWork = (data: UpdateWorkRequest) => {
  return request.put<Result<void>>('/works', data);
};

// 删除作品
export const deleteWork = (workId: string) => {
  return request.delete<Result<void>>(`/works/${workId}`);
};

// 分页查询作品列表
export const getWorkList = (pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>('/works', {
    params: { pageNum, pageSize }
  });
};

// 查询用户的作品列表
export const getUserWorks = (userId: string, pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>(`/works/user/${userId}`, {
    params: { pageNum, pageSize }
  });
};

// 作品排行榜
export const getRankingList = (limit: number = 20) => {
  return request.get<Result<WorkVO[]>>('/works/ranking', {
    params: { limit }
  });
};
