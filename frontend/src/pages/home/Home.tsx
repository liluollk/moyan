import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Row, Col, Spin, Typography } from 'antd';
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
const PAGE_SIZE = 10;

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [works, setWorks] = useState<WorkVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [currentUserId, setCurrentUserId] = useState<string | undefined>();
  const pageRef = useRef(1);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const loadWorks = useCallback(async (page: number) => {
    setLoading(true);
    try {
      const response = await getWorkList(page, PAGE_SIZE);
      const { records, total } = response.data.data;
      if (page === 1) {
        setWorks(records);
      } else {
        setWorks(prev => [...prev, ...records]);
      }
      setHasMore(records.length === PAGE_SIZE && works.length + records.length < total);
      pageRef.current = page;
    } catch {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadWorks(1);
    getCurrentUserInfo().then(res => {
      if (res.data.code === 200) setCurrentUserId(res.data.data.id);
    }).catch(() => {});
  }, []);

  // IntersectionObserver 监听底部哨兵
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && hasMore && !loading) {
          loadWorks(pageRef.current + 1);
        }
      },
      { threshold: 0.1 }
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasMore, loading, loadWorks]);

  const handleLike = async (workId: string) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;
    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;
    setWorks(prev => prev.map(w =>
      w.id === workId ? { ...w, isLiked: !wasLiked, likeCount: wasLiked ? oldCount - 1 : oldCount + 1 } : w
    ));
    try {
      if (wasLiked) { await unlikeWork(workId); } else { await likeWork(workId); }
    } catch {
      setWorks(prev => prev.map(w =>
        w.id === workId ? { ...w, isLiked: wasLiked, likeCount: oldCount } : w
      ));
    }
  };

  const handleFavorite = async (workId: string) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;
    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;
    setWorks(prev => prev.map(w =>
      w.id === workId ? { ...w, isFavorited: !wasFavorited, favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1 } : w
    ));
    try {
      if (wasFavorited) { await unfavoriteWork(workId); } else { await favoriteWork(workId); }
    } catch {
      setWorks(prev => prev.map(w =>
        w.id === workId ? { ...w, isFavorited: wasFavorited, favoriteCount: oldCount } : w
      ));
    }
  };

  const handleFollow = async (userId: string) => {
    const target = works.find(w => w.userId === userId);
    if (!target) return;
    const wasFollowing = target.isFollowing;
    setWorks(prev => prev.map(w =>
      w.userId === userId ? { ...w, isFollowing: !wasFollowing } : w
    ));
    try {
      if (wasFollowing) { await unfollowUser(userId); } else { await followUser(userId); }
    } catch {
      setWorks(prev => prev.map(w =>
        w.userId === userId ? { ...w, isFollowing: wasFollowing } : w
      ));
    }
  };

  const handleClick = (workId: string) => navigate(`/work/${workId}`);

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 24 }}>
        <FireOutlined style={{ color: '#667eea', fontSize: 20 }} />
        <Text strong style={{ fontSize: 18, color: '#1a1a2e' }}>最新作品</Text>
      </div>

      <Row gutter={[16, 16]}>
        {works.map(work => (
          <Col span={24} key={work.id}>
            <WorkCard work={work} onLike={handleLike} onFavorite={handleFavorite}
              onFollow={handleFollow} onClick={handleClick} currentUserId={currentUserId} />
          </Col>
        ))}
      </Row>

      <div ref={sentinelRef} style={{ textAlign: 'center', padding: '24px 0' }}>
        {loading && <Spin />}
        {!hasMore && works.length > 0 && (
          <Text type="secondary" style={{ fontSize: 13 }}>— 已经到底了 —</Text>
        )}
      </div>
    </div>
  );
};

export default HomePage;
