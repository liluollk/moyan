import request from '../utils/request';
import type { Result, PageResult, CommentVO, AddCommentRequest } from '../types';

// 添加评论
export const addComment = (data: AddCommentRequest) => {
  return request.post<Result<CommentVO>>('/comments', data);
};

// 删除评论
export const deleteComment = (commentId: number) => {
  return request.delete<Result<void>>(`/comments/${commentId}`);
};

// 获取作品评论列表
export const getComments = (workId: number, pageNum: number = 1, pageSize: number = 10) => {
  return request.get<Result<PageResult<CommentVO>>>(`/comments/work/${workId}`, {
    params: { pageNum, pageSize }
  });
};
