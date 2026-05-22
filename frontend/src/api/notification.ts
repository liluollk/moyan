import request from '../utils/request';
import type { Result, Notification, PageResult } from '../types';

// 获取未读通知数
export const getUnreadCount = () => {
  return request.get<Result<number>>('/notifications/unread-count');
};

// 获取通知列表
export const getNotificationList = (pageNum: number = 1, pageSize: number = 20) => {
  return request.get<Result<PageResult<Notification>>>('/notifications', {
    params: { pageNum, pageSize }
  });
};
