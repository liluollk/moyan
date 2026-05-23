import React, { useState, useEffect } from 'react';
import { Card, Avatar, Typography, Spin, Empty, message } from 'antd';
import { TrophyOutlined, HeartOutlined, HeartFilled, StarOutlined, StarFilled, MessageOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getRankingList } from '../../api/work';
import { likeWork, unlikeWork } from '../../api/like';
import { favoriteWork, unfavoriteWork } from '../../api/favorite';
import type { WorkVO } from '../../types';

const { Text, Title } = Typography;

const rankColors = ['#faad14', '#bfbfbf', '#cd6839'];

const RankingPage: React.FC = () => {
  const navigate = useNavigate();
  const [works, setWorks] = useState<WorkVO[]>([]);
  const [loading, setLoading] = useState(false);

  const loadRanking = async () => {
    setLoading(true);
    try {
      const response = await getRankingList(20);
      setWorks(response.data.data);
    } catch {
      message.error('加载排行榜失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadRanking(); }, []);

  const handleLike = async (work: WorkVO) => {
    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;
    setWorks(prev => prev.map(w => w.id === work.id ? { ...w, isLiked: !wasLiked, likeCount: wasLiked ? oldCount - 1 : oldCount + 1 } : w));
    try {
      if (wasLiked) { await unlikeWork(work.id); }
      else { await likeWork(work.id); }
    } catch {
      setWorks(prev => prev.map(w => w.id === work.id ? { ...w, isLiked: wasLiked, likeCount: oldCount } : w));
      message.error('操作失败');
    }
  };

  const handleFavorite = async (work: WorkVO) => {
    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;
    setWorks(prev => prev.map(w => w.id === work.id ? { ...w, isFavorited: !wasFavorited, favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1 } : w));
    try {
      if (wasFavorited) { await unfavoriteWork(work.id); }
      else { await favoriteWork(work.id); }
    } catch {
      setWorks(prev => prev.map(w => w.id === work.id ? { ...w, isFavorited: wasFavorited, favoriteCount: oldCount } : w));
      message.error('操作失败');
    }
  };

  const scoreOf = (w: WorkVO) => (w.likeCount || 0) * 3 + (w.favoriteCount || 0) * 2 + (w.commentCount || 0);

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <div style={{
          width: 44, height: 44, borderRadius: 14,
          background: 'linear-gradient(135deg, #faad14 0%, #fa8c16 100%)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 4px 12px rgba(250,173,20,0.3)',
        }}>
          <TrophyOutlined style={{ color: 'white', fontSize: 20 }} />
        </div>
        <div>
          <Title level={4} style={{ margin: 0, color: '#1a1a2e' }}>作品排行榜</Title>
          <Text type="secondary" style={{ fontSize: 13 }}>综合热度排名</Text>
        </div>
      </div>

      <Spin spinning={loading}>
        {works.length > 0 ? (
          <Card style={{ borderRadius: 20 }}>
            {works.map((work, index) => {
              const rank = index + 1;
              const isTop3 = rank <= 3;
              return (
                <div
                  key={work.id}
                  onClick={() => navigate(`/work/${work.id}`)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 16,
                    padding: '16px 8px',
                    borderBottom: index < works.length - 1 ? '1px solid #f5f5f5' : 'none',
                    cursor: 'pointer',
                    borderRadius: 12,
                    transition: 'background 0.2s',
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#fafafa'}
                  onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                >
                  {/* 排名 */}
                  <div style={{
                    width: 36,
                    height: 36,
                    borderRadius: 10,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 700,
                    fontSize: isTop3 ? 18 : 15,
                    color: isTop3 ? 'white' : '#999',
                    background: isTop3
                      ? rank === 1 ? 'linear-gradient(135deg, #faad14 0%, #fa8c16 100%)'
                      : rank === 2 ? 'linear-gradient(135deg, #bfbfbf 0%, #8c8c8c 100%)'
                      : 'linear-gradient(135deg, #cd6839 0%, #a0522d 100%)'
                      : '#f5f5f5',
                    flexShrink: 0,
                  }}>
                    {rank}
                  </div>

                  {/* 作品封面 */}
                  {work.images && work.images.length > 0 && (
                    <img
                      src={work.images[0]}
                      alt={work.title}
                      style={{ width: 64, height: 64, borderRadius: 12, objectFit: 'cover', flexShrink: 0 }}
                    />
                  )}

                  {/* 信息 */}
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <Text strong style={{ fontSize: 15, display: 'block', marginBottom: 4, color: '#1a1a2e' }}
                      ellipsis={{ tooltip: work.title }}>
                      {work.title}
                    </Text>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <Avatar src={work.avatar} size={22} icon={!work.avatar && <UserOutlined />} />
                      <Text type="secondary" style={{ fontSize: 13 }}>{work.nickname}</Text>
                    </div>
                  </div>

                  {/* 热度分数 */}
                  <div style={{ textAlign: 'right', flexShrink: 0, marginRight: 8 }}>
                    <Text style={{ fontSize: 18, fontWeight: 700, color: isTop3 ? rankColors[index] : '#ccc' }}>
                      {scoreOf(work)}
                    </Text>
                    <Text type="secondary" style={{ fontSize: 11, display: 'block' }}>热度</Text>
                  </div>

                  {/* 互动数据 */}
                  <div style={{ display: 'flex', gap: 16, flexShrink: 0 }}>
                    <div
                      onClick={(e) => { e.stopPropagation(); handleLike(work); }}
                      style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
                    >
                      {work.isLiked
                        ? <HeartFilled style={{ color: '#ff4d4f', fontSize: 16 }} />
                        : <HeartOutlined style={{ color: '#bbb', fontSize: 16 }} />}
                      <Text style={{ fontSize: 13, color: work.isLiked ? '#ff4d4f' : '#999' }}>
                        {work.likeCount || 0}
                      </Text>
                    </div>
                    <div
                      onClick={(e) => { e.stopPropagation(); handleFavorite(work); }}
                      style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
                    >
                      {work.isFavorited
                        ? <StarFilled style={{ color: '#faad14', fontSize: 16 }} />
                        : <StarOutlined style={{ color: '#bbb', fontSize: 16 }} />}
                      <Text style={{ fontSize: 13, color: work.isFavorited ? '#faad14' : '#999' }}>
                        {work.favoriteCount || 0}
                      </Text>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <MessageOutlined style={{ color: '#bbb', fontSize: 16 }} />
                      <Text style={{ fontSize: 13, color: '#999' }}>{work.commentCount || 0}</Text>
                    </div>
                  </div>
                </div>
              );
            })}
          </Card>
        ) : (
          !loading && <Empty description="暂无排行数据" imageStyle={{ height: 80 }} />
        )}
      </Spin>
    </div>
  );
};

export default RankingPage;
