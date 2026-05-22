import React, { useState, useEffect } from 'react';
import { Row, Col, Pagination, Empty, Spin, Typography } from 'antd';
import { FireOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getWorkList } from '../../api/work';
import { likeWork, unlikeWork } from '../../api/like';
import { favoriteWork, unfavoriteWork } from '../../api/favorite';
import { followUser, unfollowUser } from '../../api/follow';
import { getCurrentUserInfo } from '../../api/auth';
import WorkCard from '../../components/work/WorkCard';
import type { WorkVO } from '../../types';
import { message } from 'antd';

const { Text } = Typography;

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [works, setWorks] = useState<WorkVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [currentUserId, setCurrentUserId] = useState<number | undefined>();
  const pageSize = 10;

  // 加载作品列表
  const loadWorks = async (page: number) => {
    setLoading(true);
    try {
      const response = await getWorkList(page, pageSize);
      setWorks(response.data.data.records);
      setTotal(response.data.data.total);
      setCurrentPage(page);
    } catch (error: any) {
      message.error('加载作品失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWorks(1);
    // 获取当前用户ID
    getCurrentUserInfo().then(res => {
      if (res.data.code === 200) {
        setCurrentUserId(res.data.data.id);
      }
    }).catch(() => {});
  }, []);

  // 处理点赞（乐观更新）
  const handleLike = async (workId: number) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;

    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;

    // 先更新本地状态
    setWorks(prev => prev.map(w =>
      w.id === workId
        ? {
            ...w,
            isLiked: !wasLiked,
            likeCount: wasLiked ? oldCount - 1 : oldCount + 1,
          }
        : w
    ));

    try {
      if (wasLiked) {
        await unlikeWork(workId);
        message.success('已取消点赞');
      } else {
        await likeWork(workId);
        message.success('点赞成功');
      }
    } catch (error: any) {
      // 失败时回滚
      setWorks(prev => prev.map(w =>
        w.id === workId
          ? { ...w, isLiked: wasLiked, likeCount: oldCount }
          : w
      ));
      message.error('操作失败');
    }
  };

  // 处理收藏（乐观更新）
  const handleFavorite = async (workId: number) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;

    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;

    // 先更新本地状态
    setWorks(prev => prev.map(w =>
      w.id === workId
        ? {
            ...w,
            isFavorited: !wasFavorited,
            favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1,
          }
        : w
    ));

    try {
      if (wasFavorited) {
        await unfavoriteWork(workId);
        message.success('已取消收藏');
      } else {
        await favoriteWork(workId);
        message.success('收藏成功');
      }
    } catch (error: any) {
      // 失败时回滚
      setWorks(prev => prev.map(w =>
        w.id === workId
          ? { ...w, isFavorited: wasFavorited, favoriteCount: oldCount }
          : w
      ));
      message.error('操作失败');
    }
  };

  // 点击作品卡片
  const handleClick = (workId: number) => {
    navigate(`/work/${workId}`);
  };

  // 处理关注（乐观更新）
  const handleFollow = async (userId: number) => {
    const work = works.find(w => w.userId === userId);
    if (!work) return;

    const wasFollowing = work.isFollowing;

    // 先更新本地状态（更新所有该用户的作品）
    setWorks(prev => prev.map(w =>
      w.userId === userId ? { ...w, isFollowing: !wasFollowing } : w
    ));

    try {
      if (wasFollowing) {
        await unfollowUser(userId);
        message.success('已取消关注');
      } else {
        await followUser(userId);
        message.success('关注成功');
      }
    } catch (error: any) {
      // 失败时回滚
      setWorks(prev => prev.map(w =>
        w.userId === userId ? { ...w, isFollowing: wasFollowing } : w
      ));
      message.error('操作失败');
    }
  };

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 24 }}>
        <FireOutlined style={{ color: '#667eea', fontSize: 20 }} />
        <Text strong style={{ fontSize: 18, color: '#1a1a2e' }}>最新作品</Text>
      </div>
      <Spin spinning={loading}>
        {works.length > 0 ? (
          <>
            <Row gutter={[16, 16]}>
              {works.map((work) => (
                <Col span={24} key={work.id}>
                  <WorkCard
                    work={work}
                    onLike={handleLike}
                    onFavorite={handleFavorite}
                    onFollow={handleFollow}
                    onClick={handleClick}
                    currentUserId={currentUserId}
                  />
                </Col>
              ))}
            </Row>

            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination
                current={currentPage}
                total={total}
                pageSize={pageSize}
                onChange={loadWorks}
              />
            </div>
          </>
        ) : (
          !loading && <Empty description="暂无作品" />
        )}
      </Spin>
    </div>
  );
};

export default HomePage;
