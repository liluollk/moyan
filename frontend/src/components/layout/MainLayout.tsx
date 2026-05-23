import React, { useState, useEffect } from 'react';
import { Layout, Menu, Badge, Input, Avatar, Dropdown } from 'antd';
import {
  HomeOutlined,
  CompassOutlined,
  PlusCircleOutlined,
  BellOutlined,
  SearchOutlined,
  UserOutlined,
  LogoutOutlined,
  HeartOutlined,
  TrophyOutlined,
  RobotOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { getUnreadCount } from '../../api/notification';
import { logout } from '../../api/auth';
import { message } from 'antd';
import type { MenuProps } from 'antd';

const { Header, Content } = Layout;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    const fetchUnreadCount = async () => {
      try {
        const response = await getUnreadCount();
        setUnreadCount(response.data.data);
      } catch (error) {
        // ignore network errors
      }
    };

    if (localStorage.getItem('accessToken')) {
      fetchUnreadCount();
      const interval = setInterval(fetchUnreadCount, 30000);
      return () => clearInterval(interval);
    }
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      // logout API failure is non-critical
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      message.success('已退出登录');
      navigate('/login');
    }
  };

  const menuItems = [
    { key: '/', icon: <HomeOutlined />, label: '首页' },
    { key: '/follow', icon: <HeartOutlined />, label: '关注' },
    { key: '/recommend', icon: <CompassOutlined />, label: '推荐' },
    { key: '/ranking', icon: <TrophyOutlined />, label: '排行榜' },
    { key: '/publish', icon: <PlusCircleOutlined />, label: '发布' },
    { key: '/ai', icon: <RobotOutlined />, label: 'AI问答' }
  ];

  const userMenuItems: MenuProps['items'] = [
    {
      key: '/profile',
      icon: <UserOutlined style={{ fontSize: 14 }} />,
      label: '个人中心',
    },
    { type: 'divider' },
    {
      key: 'logout',
      icon: <LogoutOutlined style={{ fontSize: 14, color: '#ff4d4f' }} />,
      label: <span style={{ color: '#ff4d4f' }}>退出登录</span>,
      danger: true,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
      {/* 顶部渐变装饰条 */}
      <div className="page-top-accent" />

      {/* 背景大光晕 */}
      <div className="bg-blob-1" />
      <div className="bg-blob-2" />
      <div className="bg-blob-3" />

      {/* 浮动装饰圆环 */}
      <div className="deco-ring deco-ring-1" />
      <div className="deco-ring deco-ring-2" />
      <div className="deco-ring deco-ring-3" />
      <div className="deco-ring deco-ring-4" />

      {/* 实心装饰圆点 */}
      <div className="deco-dot deco-dot-1" />
      <div className="deco-dot deco-dot-2" />
      <div className="deco-dot deco-dot-3" />
      <div className="deco-dot deco-dot-4" />

      <Header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        background: 'rgba(255, 255, 255, 0.88)',
        padding: '0 48px',
        boxShadow: '0 1px 20px rgba(0,0,0,0.06)',
        backdropFilter: 'blur(20px)',
        position: 'sticky',
        top: 0,
        zIndex: 1000,
        height: 64,
        borderBottom: '1px solid rgba(102, 126, 234, 0.08)'
      }}>
        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              marginRight: 40,
              cursor: 'pointer',
              gap: 10
            }}
            onClick={() => navigate('/')}
          >
            <span style={{
              fontSize: 22,
              fontWeight: 800,
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              letterSpacing: 2
            }}>
              墨言
            </span>
          </div>

          <Menu
            mode="horizontal"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
            style={{
              border: 'none',
              flex: 1,
              background: 'transparent',
              minWidth: 360
            }}
          />
        </div>

        {/* Right side */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Input
            placeholder="搜索创意..."
            prefix={<SearchOutlined style={{ color: '#bbb', fontSize: 14 }} />}
            style={{
              width: 220,
              borderRadius: 20,
              height: 36,
              background: '#f5f5f9',
              border: '1.5px solid transparent',
              fontSize: 13
            }}
            onFocus={(e) => {
              e.target.style.background = '#fff';
              e.target.style.borderColor = '#667eea';
            }}
            onBlur={(e) => {
              e.target.style.background = '#f5f5f9';
              e.target.style.borderColor = 'transparent';
            }}
            onPressEnter={(e) => {
              const value = (e.target as HTMLInputElement).value;
              if (value.trim()) {
                navigate(`/search?keyword=${encodeURIComponent(value.trim())}`);
              }
            }}
          />

          <Badge count={unreadCount} overflowCount={99} size="small">
            <div
              onClick={() => {
                setUnreadCount(0);
                navigate('/notifications');
              }}
              style={{
                height: 36,
                width: 36,
                borderRadius: 12,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: location.pathname === '/notifications'
                  ? 'rgba(102, 126, 234, 0.12)' : '#f5f5f9',
                cursor: 'pointer',
                transition: 'all 0.2s'
              }}
            >
              <BellOutlined style={{
                fontSize: 16,
                color: unreadCount > 0 ? '#667eea'
                  : location.pathname === '/notifications' ? '#667eea' : '#888'
              }} />
            </div>
          </Badge>

          <Dropdown menu={{
            items: userMenuItems,
            onClick: ({ key }) => {
              if (key === 'logout') handleLogout();
              else navigate(key);
            }
          }} placement="bottomRight" trigger={['click']}>
            <Avatar
              size={36}
              icon={<UserOutlined />}
              style={{
                cursor: 'pointer',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                border: '2px solid rgba(102, 126, 234, 0.2)',
                transition: 'all 0.2s'
              }}
            />
          </Dropdown>
        </div>
      </Header>

      <Content style={{
        padding: '28px 24px',
        background: 'transparent',
        minHeight: 'calc(100vh - 64px)'
      }}>
        <div className="fade-in">
          <Outlet />
        </div>
      </Content>
    </Layout>
  );
};

export default MainLayout;
