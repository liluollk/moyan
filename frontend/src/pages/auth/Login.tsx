import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { LockOutlined, PhoneOutlined, EditOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../../api/auth';
import type { LoginRequest } from '../../types';

const { Title } = Typography;

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: LoginRequest) => {
    setLoading(true);
    try {
      const response = await login(values);
      const { accessToken, refreshToken } = response.data.data;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      message.success('登录成功');
      navigate('/');
    } catch (error: any) {
      message.error(error.response?.data?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* 动态渐变背景 */}
      <div className="auth-bg" />

      {/* 浮动装饰元素 */}
      <div className="auth-orb auth-orb-1" />
      <div className="auth-orb auth-orb-2" />
      <div className="auth-orb auth-orb-3" />
      <div className="auth-ring auth-ring-1" />
      <div className="auth-ring auth-ring-2" />
      <div className="auth-dot auth-dot-1" />
      <div className="auth-dot auth-dot-2" />
      <div className="auth-dot auth-dot-3" />

      <div className="auth-content">
        {/* Logo */}
        <div className="auth-logo">
          <div className="auth-logo-icon">
            <EditOutlined style={{ fontSize: 28, color: 'white' }} />
          </div>
          <Title level={2} style={{
            color: 'white',
            margin: '12px 0 4px',
            textShadow: '0 2px 16px rgba(0,0,0,0.15)',
            fontWeight: 700,
            letterSpacing: 6,
          }}>墨言</Title>
        </div>

        {/* 卡片 */}
        <Card
          className="auth-card"
          bodyStyle={{ padding: '36px 32px' }}
        >
          <Form
            name="login"
            onFinish={onFinish}
            autoComplete="off"
            size="large"
          >
            <Form.Item
              name="phone"
              rules={[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
              ]}
            >
              <Input
                prefix={<PhoneOutlined style={{ color: '#667eea', fontSize: 16 }} />}
                placeholder="请输入手机号"
                className="auth-input"
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password
                prefix={<LockOutlined style={{ color: '#667eea', fontSize: 16 }} />}
                placeholder="请输入密码"
                className="auth-input"
              />
            </Form.Item>

            <Form.Item style={{ marginTop: 8 }}>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                block
                className="auth-btn"
              >
                登录
              </Button>
            </Form.Item>

            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <span style={{ color: '#999', fontSize: 14 }}>还没有账号？</span>
              <Link to="/register" style={{ marginLeft: 6, color: '#667eea', fontWeight: 600, fontSize: 14 }}>
                立即注册
              </Link>
            </div>
          </Form>
        </Card>

        {/* 底部标语 */}
        <p className="auth-tagline">创意作品分享平台，让灵感被看见</p>
      </div>
    </div>
  );
};

export default LoginPage;
