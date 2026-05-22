import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { LockOutlined, PhoneOutlined } from '@ant-design/icons';
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
      
      // 保存Token
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
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      padding: '20px'
    }}>
      <Card 
        style={{ 
          width: 420, 
          borderRadius: '20px',
          backdropFilter: 'blur(10px)',
          backgroundColor: 'rgba(255, 255, 255, 0.85)',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.2)',
          overflow: 'hidden'
        }}
        bodyStyle={{ padding: '40px 30px' }}
      >
        <div style={{ textAlign: 'center', marginBottom: 30 }}>
          <div style={{ 
            width: 80, 
            height: 80, 
            borderRadius: '50%', 
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 16px',
            boxShadow: '0 4px 15px rgba(102, 126, 234, 0.4)'
          }}>
            <span style={{ fontSize: 32, color: 'white', fontWeight: 'bold' }}>墨</span>
          </div>
          <Title level={2} style={{ 
            color: '#333', 
            marginBottom: 8,
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>墨言</Title>
          <Title level={4} style={{ color: '#666', fontWeight: 'normal' }}>欢迎回来</Title>
        </div>

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
              prefix={<PhoneOutlined style={{ color: '#667eea' }} />} 
              placeholder="请输入手机号"
              size="large"
              style={{ height: 48, borderRadius: 12 }}
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password 
              prefix={<LockOutlined style={{ color: '#667eea' }} />}
              placeholder="请输入密码"
              size="large"
              style={{ height: 48, borderRadius: 12 }}
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading}
              block
              size="large"
              style={{ 
                height: 48,
                borderRadius: 12,
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                borderColor: 'transparent',
                fontSize: 16,
                fontWeight: 500
              }}
            >
              登录
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <span style={{ color: '#666' }}>还没有账号？</span>
            <Link to="/register" style={{ marginLeft: 8, color: '#667eea', fontWeight: 500 }}>
              立即注册
            </Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;