import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';

// 布局组件
import MainLayout from './components/layout/MainLayout';

// 页面组件
import LoginPage from './pages/auth/Login';
import RegisterPage from './pages/auth/Register';
import HomePage from './pages/home/Home';
import FollowFeedPage from './pages/home/FollowFeed';
import RecommendFeedPage from './pages/home/RecommendFeed';
import PublishPage from './pages/work/Publish';
import WorkDetailPage from './pages/work/WorkDetail';
import NotificationsPage from './pages/notification/Notifications';
import SearchPage from './pages/search/Search';
import ProfilePage from './pages/user/Profile';

// 路由守卫组件
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const token = localStorage.getItem('accessToken');
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          {/* 公开路由 */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          {/* 受保护路由 */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<HomePage />} />
            <Route path="follow" element={<FollowFeedPage />} />
            <Route path="recommend" element={<RecommendFeedPage />} />
            <Route path="publish" element={<PublishPage />} />
            <Route path="work/:workId" element={<WorkDetailPage />} />
            <Route path="notifications" element={<NotificationsPage />} />
            <Route path="search" element={<SearchPage />} />
            <Route path="profile" element={<ProfilePage />} />
          </Route>
          
          {/* 重定向未知路由 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
};

export default App;
