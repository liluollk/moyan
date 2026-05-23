import React, { useState, useEffect } from 'react';
import { Card, Avatar, Button, Modal, Form, Input, message, Spin, Typography, Tabs, Empty } from 'antd';
import { UserOutlined, EditOutlined, PhoneOutlined, CalendarOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getCurrentUserInfo, updateProfile } from '../../api/auth';
import { getUserWorks } from '../../api/work';
import { getMyFavorites } from '../../api/favorite';
import { getFollowers, getFollowing, followUser, unfollowUser } from '../../api/follow';
import { likeWork, unlikeWork } from '../../api/like';
import { favoriteWork, unfavoriteWork } from '../../api/favorite';
import WorkCard from '../../components/work/WorkCard';
import type { User, WorkVO, FollowUserVO } from '../../types';

const { TextArea } = Input;
const { Title, Text } = Typography;

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editLoading, setEditLoading] = useState(false);
  const [form] = Form.useForm();

  // Tab data
  const [myWorks, setMyWorks] = useState<WorkVO[]>([]);
  const [myFavorites, setMyFavorites] = useState<WorkVO[]>([]);
  const [followers, setFollowers] = useState<FollowUserVO[]>([]);
  const [following, setFollowing] = useState<FollowUserVO[]>([]);
  const [tabLoading, setTabLoading] = useState(false);

  useEffect(() => { fetchUserInfo(); }, []);

  const fetchUserInfo = async () => {
    try {
      setLoading(true);
      const res = await getCurrentUserInfo();
      if (res.data.code === 200) {
        setUser(res.data.data);
        loadMyWorks(res.data.data.id);
      }
    } catch {
      message.error('获取用户信息失败');
    } finally {
      setLoading(false);
    }
  };

  const loadMyWorks = async (userId: string) => {
    setTabLoading(true);
    try {
      const res = await getUserWorks(userId, 1, 50);
      setMyWorks(res.data.data.records || []);
    } catch {} finally { setTabLoading(false); }
  };

  const loadMyFavorites = async () => {
    if (myFavorites.length > 0) return;
    setTabLoading(true);
    try {
      const res = await getMyFavorites();
      setMyFavorites(res.data.data || []);
    } catch {} finally { setTabLoading(false); }
  };

  const loadFollowers = async () => {
    if (!user || followers.length > 0) return;
    setTabLoading(true);
    try {
      const res = await getFollowers(user.id);
      setFollowers(res.data.data || []);
    } catch {} finally { setTabLoading(false); }
  };

  const loadFollowing = async () => {
    if (!user || following.length > 0) return;
    setTabLoading(true);
    try {
      const res = await getFollowing(user.id);
      setFollowing(res.data.data || []);
    } catch {} finally { setTabLoading(false); }
  };

  const handleTabChange = (key: string) => {
    if (key === 'favorites') loadMyFavorites();
    else if (key === 'followers') loadFollowers();
    else if (key === 'following') loadFollowing();
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
      message.error('更新失败');
    } finally { setEditLoading(false); }
  };

  const handleLike = async (workId: string) => {
    const work = myWorks.find(w => w.id === workId) || myFavorites.find(w => w.id === workId);
    if (!work) return;
    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;
    const update = (w: WorkVO) => w.id === workId ? { ...w, isLiked: !wasLiked, likeCount: wasLiked ? oldCount - 1 : oldCount + 1 } : w;
    setMyWorks(prev => prev.map(update));
    setMyFavorites(prev => prev.map(update));
    try {
      if (wasLiked) await unlikeWork(workId);
      else await likeWork(workId);
    } catch {
      const revert = (w: WorkVO) => w.id === workId ? { ...w, isLiked: wasLiked, likeCount: oldCount } : w;
      setMyWorks(prev => prev.map(revert));
      setMyFavorites(prev => prev.map(revert));
      message.error('操作失败');
    }
  };

  const handleFavorite = async (workId: string) => {
    const work = myWorks.find(w => w.id === workId) || myFavorites.find(w => w.id === workId);
    if (!work) return;
    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;
    const update = (w: WorkVO) => w.id === workId ? { ...w, isFavorited: !wasFavorited, favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1 } : w;
    setMyWorks(prev => prev.map(update));
    setMyFavorites(prev => prev.map(update));
    try {
      if (wasFavorited) await unfavoriteWork(workId);
      else await favoriteWork(workId);
    } catch {
      const revert = (w: WorkVO) => w.id === workId ? { ...w, isFavorited: wasFavorited, favoriteCount: oldCount } : w;
      setMyWorks(prev => prev.map(revert));
      setMyFavorites(prev => prev.map(revert));
      message.error('操作失败');
    }
  };

  const handleFollowToggle = async (targetUser: FollowUserVO) => {
    const wasFollowing = targetUser.isFollowing;
    const update = (list: FollowUserVO[]) => list.map(u => u.userId === targetUser.userId ? { ...u, isFollowing: !wasFollowing } : u);
    setFollowers(update);
    setFollowing(update);
    try {
      if (wasFollowing) await unfollowUser(targetUser.userId);
      else await followUser(targetUser.userId);
    } catch {
      const revert = (list: FollowUserVO[]) => list.map(u => u.userId === targetUser.userId ? { ...u, isFollowing: wasFollowing } : u);
      setFollowers(revert);
      setFollowing(revert);
      message.error('操作失败');
    }
  };

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  if (!user) return <div style={{ textAlign: 'center', padding: '100px' }}><Text type="secondary">请先登录</Text></div>;

  const UserList: React.FC<{ users: FollowUserVO[] }> = ({ users }) => (
    users.length > 0 ? (
      <div>
        {users.map(u => (
          <div key={u.userId} style={{ display: 'flex', alignItems: 'center', padding: '14px 8px', borderBottom: '1px solid #f5f5f5', borderRadius: 12 }}>
            <Avatar src={u.avatar} size={44} icon={!u.avatar && <UserOutlined />} style={{ cursor: 'pointer' }} onClick={() => navigate(`/user/${u.userId}`)} />
            <div style={{ marginLeft: 14, flex: 1 }}>
              <Text strong style={{ fontSize: 15, cursor: 'pointer' }} onClick={() => navigate(`/user/${u.userId}`)}>{u.nickname}</Text>
            </div>
            {user && u.userId !== user.id && (
              <Button
                size="small"
                onClick={() => handleFollowToggle(u)}
                style={{
                  borderRadius: 20,
                  fontWeight: 500,
                  background: u.isFollowing ? '#f7f8fa' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: u.isFollowing ? '#888' : 'white',
                  borderColor: u.isFollowing ? '#ddd' : 'transparent',
                }}
              >
                {u.isFollowing ? '已关注' : '关注'}
              </Button>
            )}
          </div>
        ))}
      </div>
    ) : <Empty description="暂无数据" imageStyle={{ height: 60 }} />
  );

  const WorkGrid: React.FC<{ works: WorkVO[] }> = ({ works }) => (
    works.length > 0 ? (
      <div>
        {works.map(work => (
          <WorkCard key={work.id} work={work} onLike={handleLike} onFavorite={handleFavorite} onClick={(id) => navigate(`/work/${id}`)} />
        ))}
      </div>
    ) : <Empty description="暂无内容" imageStyle={{ height: 60 }} />
  );

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      {/* Profile header */}
      <Card style={{ borderRadius: 20, overflow: 'hidden', marginBottom: 20 }}>
        <div style={{
          height: 120,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          margin: '-24px -24px 0',
          position: 'relative',
        }}>
          <div style={{
            position: 'absolute', bottom: -40, left: 24, width: 96, height: 96,
            borderRadius: 24, background: 'white', padding: 4,
            boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
          }}>
            <Avatar size={88} src={user.avatar || undefined} icon={!user.avatar ? <UserOutlined /> : undefined} style={{ borderRadius: 20 }} />
          </div>
        </div>
        <div style={{ marginTop: 52, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <Title level={3} style={{ margin: '0 0 4px', color: '#1a1a2e' }}>{user.nickname || '未设置昵称'}</Title>
            <Text type="secondary" style={{ fontSize: 14 }}>{user.intro || '这个人很懒，什么都没有留下~'}</Text>
          </div>
          <Button icon={<EditOutlined />} onClick={handleEditClick} style={{ borderRadius: 12, borderColor: '#667eea', color: '#667eea', fontWeight: 500 }}>
            编辑资料
          </Button>
        </div>
        <div style={{ display: 'flex', gap: 32, marginTop: 24, paddingTop: 20, borderTop: '1px solid #f0f0f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <PhoneOutlined style={{ color: '#667eea', fontSize: 15 }} />
            <Text style={{ color: '#666' }}>{user.phone}</Text>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <CalendarOutlined style={{ color: '#667eea', fontSize: 15 }} />
            <Text style={{ color: '#666' }}>{new Date(user.createTime).toLocaleDateString('zh-CN')}</Text>
          </div>
        </div>
      </Card>

      {/* Tabs */}
      <Card style={{ borderRadius: 20 }}>
        <Spin spinning={tabLoading}>
          <Tabs
            onChange={handleTabChange}
            items={[
              { key: 'works', label: `我的作品 (${myWorks.length})`, children: <WorkGrid works={myWorks} /> },
              { key: 'favorites', label: '我的收藏', children: <WorkGrid works={myFavorites} /> },
              { key: 'following', label: '关注', children: <UserList users={following} /> },
              { key: 'followers', label: '粉丝', children: <UserList users={followers} /> },
            ]}
          />
        </Spin>
      </Card>

      {/* Edit modal */}
      <Modal
        title={<div style={{ display: 'flex', alignItems: 'center', gap: 8 }}><EditOutlined style={{ color: '#667eea' }} /><span>编辑资料</span></div>}
        open={editModalVisible}
        onOk={handleEditSubmit}
        onCancel={() => setEditModalVisible(false)}
        confirmLoading={editLoading}
        okText="保存" cancelText="取消"
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
