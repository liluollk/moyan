import axios from 'axios';
import { message } from 'antd';
import type { Result } from '../types';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
});

// 请求拦截器 - 添加Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误
request.interceptors.response.use(
  (response) => {
    const res: Result = response.data;
    
    // 如果返回的状态码不是200，说明接口有问题
    if (res.code !== 200) {
      // Token过期或无效，清除Token并跳转到登录页
      if (res.code === 401 || res.code === 4011 || res.code === 4012) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        message.error('登录已过期，请重新登录');
        window.location.href = '/login';
      }
      
      return Promise.reject(new Error(res.message || 'Error'));
    }
    
    return response;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default request;
