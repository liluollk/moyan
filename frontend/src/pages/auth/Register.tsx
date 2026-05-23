import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { PhoneOutlined, LockOutlined, SafetyOutlined, EditOutlined } from '@ant-design/icons';
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
    <div className="auth-page">
      <div className="auth-bg" />

      <div className="auth-orb auth-orb-1" />
      <div className="auth-orb auth-orb-2" />
      <div className="auth-orb auth-orb-3" />
      <div className="auth-ring auth-ring-1" />
      <div className="auth-ring auth-ring-2" />
      <div className="auth-dot auth-dot-1" />
      <div className="auth-dot auth-dot-2" />
      <div className="auth-dot auth-dot-3" />

      <div className="auth-content">
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

        <Card
          className="auth-card"
          bodyStyle={{ padding: '36px 32px' }}
        >
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
                prefix={<PhoneOutlined style={{ color: '#667eea', fontSize: 16 }} />}
                placeholder="请输入手机号"
                className="auth-input"
              />
            </Form.Item>

            <Form.Item
              name="verifyCode"
              rules={[{ required: true, message: '请输入验证码' }]}
            >
              <Input
                prefix={<SafetyOutlined style={{ color: '#667eea', fontSize: 16 }} />}
                placeholder="请输入验证码"
                className="auth-input"
                suffix={
                  <Button
                    type="primary"
                    onClick={handleSendCode}
                    disabled={sendingCode || countdown > 0}
                    style={{
                      height: 36,
                      borderRadius: 8,
                      fontSize: 13,
                      background: countdown > 0 ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      borderColor: 'transparent'
                    }}
                  >
                    {countdown > 0 ? `${countdown}s` : '发送验证码'}
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
                prefix={<LockOutlined style={{ color: '#667eea', fontSize: 16 }} />}
                placeholder="请输入密码"
                className="auth-input"
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
                prefix={<LockOutlined style={{ color: '#667eea', fontSize: 16 }} />}
                placeholder="请再次输入密码"
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
                注册
              </Button>
            </Form.Item>

            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <span style={{ color: '#999', fontSize: 14 }}>已有账号？</span>
              <Link to="/login" style={{ marginLeft: 6, color: '#667eea', fontWeight: 600, fontSize: 14 }}>
                立即登录
              </Link>
            </div>
          </Form>
        </Card>

        <p className="auth-tagline">创意作品分享平台，让灵感被看见</p>
      </div>
    </div>
  );
};

export default RegisterPage;
