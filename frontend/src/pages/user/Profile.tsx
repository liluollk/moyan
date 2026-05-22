import React, { useState, useEffect } from 'react';
import { Card, Avatar, Button, Modal, Form, Input, message, Spin, Typography } from 'antd';
import { UserOutlined, EditOutlined, PhoneOutlined, CalendarOutlined } from '@ant-design/icons';
import { getCurrentUserInfo, updateProfile } from '../../api/auth';
import type { User } from '../../types';

const { TextArea } = Input;
const { Title, Text } = Typography;

const ProfilePage: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editLoading, setEditLoading] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => { fetchUserInfo(); }, []);

  const fetchUserInfo = async () => {
    try {
      setLoading(true);
      const res = await getCurrentUserInfo();
      if (res.data.code === 200) setUser(res.data.data);
      else message.error(res.data.message || '获取用户信息失败');
    } catch (error: any) {
      message.error(error.response?.data?.message || '获取用户信息失败');
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = () => {
    form.setFieldsValue({ nickname: user?.nickname || '', intro: user?.intro || '' });
    setEditModalVisible(true);
  };

  const handleEditSubmit = async () => {
    try {
      const values = await form.validateFields();
      setEditLoading(true);
      await updateProfile(values);
      message.success('更新成功');
      setEditModalVisible(false);
      fetchUserInfo();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.response?.data?.message || '更新失败');
    } finally {
      setEditLoading(false);
    }
  };

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  if (!user) return (
    <div style={{ textAlign: 'center', padding: '100px' }}>
      <Text type="secondary">请先登录</Text>
    </div>
  );

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      {/* Profile header */}
      <Card style={{ borderRadius: 20, overflow: 'hidden', marginBottom: 20 }}>
        {/* Banner */}
        <div style={{
          height: 120,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          margin: '-24px -24px 0',
          position: 'relative',
        }}>
          <div style={{
            position: 'absolute',
            bottom: -40,
            left: 24,
            width: 96,
            height: 96,
            borderRadius: 24,
            background: 'white',
            padding: 4,
            boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
          }}>
            <Avatar
              size={88}
              src={user.avatar || undefined}
              icon={!user.avatar ? <UserOutlined /> : undefined}
              style={{ borderRadius: 20 }}
            />
          </div>
        </div>

        <div style={{ marginTop: 52, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <Title level={3} style={{ margin: '0 0 4px', color: '#1a1a2e' }}>
              {user.nickname || '未设置昵称'}
            </Title>
            <Text type="secondary" style={{ fontSize: 14 }}>
              {user.intro || '这个人很懒，什么都没有留下~'}
            </Text>
          </div>
          <Button
            icon={<EditOutlined />}
            onClick={handleEditClick}
            style={{
              borderRadius: 12,
              borderColor: '#667eea',
              color: '#667eea',
              fontWeight: 500,
            }}
          >
            编辑资料
          </Button>
        </div>

        <div style={{
          display: 'flex',
          gap: 32,
          marginTop: 24,
          paddingTop: 20,
          borderTop: '1px solid #f0f0f0',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <PhoneOutlined style={{ color: '#667eea', fontSize: 15 }} />
            <Text style={{ color: '#666' }}>{user.phone}</Text>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <CalendarOutlined style={{ color: '#667eea', fontSize: 15 }} />
            <Text style={{ color: '#666' }}>
              {new Date(user.createTime).toLocaleDateString('zh-CN')}
            </Text>
          </div>
        </div>
      </Card>

      {/* Edit modal */}
      <Modal
        title={
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <EditOutlined style={{ color: '#667eea' }} />
            <span>编辑资料</span>
          </div>
        }
        open={editModalVisible}
        onOk={handleEditSubmit}
        onCancel={() => setEditModalVisible(false)}
        confirmLoading={editLoading}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ style: { borderRadius: 10, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', border: 'none' } }}
        cancelButtonProps={{ style: { borderRadius: 10 } }}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 20 }}>
          <Form.Item name="nickname" label="昵称" rules={[{ max: 50, message: '昵称不能超过50个字符' }]}>
            <Input placeholder="请输入昵称" style={{ borderRadius: 10 }} prefix={<UserOutlined style={{ color: '#bbb' }} />} />
          </Form.Item>
          <Form.Item name="intro" label="个人简介" rules={[{ max: 500, message: '个人简介不能超过500个字符' }]}>
            <TextArea rows={4} placeholder="介绍一下自己吧" style={{ borderRadius: 10 }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ProfilePage;
