import axios from 'axios';
import { message } from 'antd';
import type { Result, TokenVO } from '../types';

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

// Token 刷新状态
let isRefreshing = false;
let pendingRequests: Array<(token: string) => void> = [];

// 响应拦截器 - 处理错误 + Token 无感刷新
request.interceptors.response.use(
  (response) => {
    const res: Result = response.data;

    if (res.code !== 200) {
      if (res.code === 401 || res.code === 4011 || res.code === 4012) {
        return handleTokenRefresh(response.config) as any;
      }

      return Promise.reject(new Error(res.message || 'Error'));
    }

    return response;
  },
  (error) => {
    // HTTP 401 状态码
    if (error.response?.status === 401) {
      return handleTokenRefresh(error.config) as any;
    }
    return Promise.reject(error);
  }
);

function handleTokenRefresh(originalConfig: any) {
  const refreshToken = localStorage.getItem('refreshToken');

  if (!refreshToken) {
    redirectToLogin();
    return Promise.reject(new Error('未登录'));
  }

  // 避免刷新接口本身也走刷新逻辑
  if (originalConfig.url?.includes('/auth/refresh')) {
    redirectToLogin();
    return Promise.reject(new Error('Token 刷新失败'));
  }

  if (!isRefreshing) {
    isRefreshing = true;

    return axios.post<Result<TokenVO>>('/api/auth/refresh', null, {
      params: { refreshToken },
      headers: { Authorization: '' }
    }).then((res) => {
      if (res.data.code === 200) {
        const { accessToken, refreshToken: newRefreshToken } = res.data.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        // 重放队列中的请求
        pendingRequests.forEach(cb => cb(accessToken));
        pendingRequests = [];

        // 重放当前请求
        originalConfig.headers.Authorization = `Bearer ${accessToken}`;
        return request(originalConfig);
      }
      throw new Error('刷新失败');
    }).catch(() => {
      redirectToLogin();
      return Promise.reject(new Error('Token 刷新失败'));
    }).finally(() => {
      isRefreshing = false;
    });
  }

  // 正在刷新中，将请求加入队列等待
  return new Promise((resolve) => {
    pendingRequests.push((newToken: string) => {
      originalConfig.headers.Authorization = `Bearer ${newToken}`;
      resolve(request(originalConfig));
    });
  });
}

function redirectToLogin() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  message.error('登录已过期，请重新登录');
  window.location.href = '/login';
}

export default request;
