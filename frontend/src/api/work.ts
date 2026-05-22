import request from '../utils/request';
import type { Result, WorkVO, PublishWorkRequest, UpdateWorkRequest, PageResult } from '../types';

// 发布作品
export const publishWork = (data: PublishWorkRequest) => {
  return request.post<Result<number>>('/works', data);
};

// 获取作品详情
export const getWorkDetail = (workId: number) => {
  return request.get<Result<WorkVO>>(`/works/${workId}`);
};

// 更新作品
export const updateWork = (data: UpdateWorkRequest) => {
  return request.put<Result<void>>('/works', data);
};

// 删除作品
export const deleteWork = (workId: number) => {
  return request.delete<Result<void>>(`/works/${workId}`);
};

// 分页查询作品列表
export const getWorkList = (pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>('/works', {
    params: { pageNum, pageSize }
  });
};

// 查询用户的作品列表
export const getUserWorks = (userId: number, pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<WorkVO>>>(`/works/user/${userId}`, {
    params: { pageNum, pageSize }
  });
};
