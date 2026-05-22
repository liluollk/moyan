import request from '../utils/request';
import type { Result } from '../types';

// 上传文件
export const uploadFile = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return request.post<Result<string>>('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};
