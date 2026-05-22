import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { PhoneOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { register, sendVerifyCode } from '../../api/auth';
import type { RegisterRequest } from '../../types';

const { Title } = Typography;

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [form] = Form.useForm();

  const handleSendCode = async () => {
    const phone = form.getFieldValue('phone');
    if (!phone) {
      message.warning('请先输入手机号');
      return;
    }

    if (!/^1[3-9]\d{9}$/.test(phone)) {
      message.warning('手机号格式不正确');
      return;
    }

    setSendingCode(true);
    try {
      await sendVerifyCode(phone);
      message.success('验证码已发送，请查看后端控制台');
      
      // 开始倒计时
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error: any) {
      message.error(error.response?.data?.message || '发送验证码失败');
    } finally {
      setSendingCode(false);
    }
  };

  const onFinish = async (values: RegisterRequest) => {
    setLoading(true);
    try {
      await register(values);
      message.success('注册成功');
      navigate('/login');
    } catch (error: any) {
      message.error(error.response?.data?.message || '注册失败');
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
          <Title level={4} style={{ color: '#666', fontWeight: 'normal' }}>创建账户</Title>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="phone"
            rules={[{ required: true, message: '请输入手机号' },
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
            name="verifyCode"
            rules={[{ required: true, message: '请输入验证码' }]}
          >
            <Input 
              prefix={<SafetyOutlined style={{ color: '#667eea' }} />} 
              placeholder="请输入验证码"
              size="large"
              style={{ height: 48, borderRadius: 12 }}
              suffix={
                <Button 
                  type="primary"
                  onClick={handleSendCode} 
                  disabled={sendingCode || countdown > 0}
                  style={{ 
                    height: 40,
                    borderRadius: 8,
                    background: countdown > 0 ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    borderColor: 'transparent'
                  }}
                >
                  {countdown > 0 ? `${countdown}s后重发` : '发送验证码'}
                </Button>
              }
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6位' }
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined style={{ color: '#667eea' }} />} 
              placeholder="请输入密码"
              size="large"
              style={{ height: 48, borderRadius: 12 }}
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            rules={[{ required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined style={{ color: '#667eea' }} />} 
              placeholder="请再次输入密码"
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
              注册
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <span style={{ color: '#666' }}>已有账号？</span>
            <Link to="/login" style={{ marginLeft: 8, color: '#667eea', fontWeight: 500 }}>
              立即登录
            </Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage;