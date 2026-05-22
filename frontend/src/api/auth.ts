import request from '../utils/request';
import type { Result, TokenVO, LoginRequest, RegisterRequest, UpdateProfileRequest, User } from '../types';

// 发送验证码
export const sendVerifyCode = (phone: string) => {
  return request.post<Result<void>>('/auth/send-code', null, {
    params: { phone }
  });
};

// 用户注册
export const register = (data: RegisterRequest) => {
  return request.post<Result<TokenVO>>('/auth/register', data);
};

// 用户登录
export const login = (data: LoginRequest) => {
  return request.post<Result<TokenVO>>('/auth/login', data);
};

// 刷新Token
export const refreshToken = (refreshToken: string) => {
  return request.post<Result<TokenVO>>('/auth/refresh', null, {
    params: { refreshToken }
  });
};

// 注销
export const logout = () => {
  return request.post<Result<void>>('/auth/logout');
};

// 获取当前用户信息
export const getCurrentUserInfo = () => {
  return request.get<Result<User>>('/auth/me');
};

// 更新用户资料
export const updateProfile = (data: UpdateProfileRequest) => {
  return request.put<Result<void>>('/users/profile', data);
};
