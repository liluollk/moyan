import React, { useState, useEffect } from 'react';
import { Row, Col, Pagination, Empty, Spin, Typography } from 'antd';
import { CompassOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getRecommendFeed } from '../../api/feed';
import { likeWork, unlikeWork } from '../../api/like';
import { favoriteWork, unfavoriteWork } from '../../api/favorite';
import WorkCard from '../../components/work/WorkCard';
import type { WorkVO } from '../../types';
import { message } from 'antd';

const RecommendFeedPage: React.FC = () => {
  const navigate = useNavigate();
  const [works, setWorks] = useState<WorkVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;

  const loadWorks = async (page: number) => {
    setLoading(true);
    try {
      const response = await getRecommendFeed(page, pageSize);
      setWorks(response.data.data.records);
      setTotal(response.data.data.total);
      setCurrentPage(page);
    } catch (error: any) {
      message.error('加载推荐动态失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWorks(1);
  }, []);

  const handleLike = async (workId: number) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;

    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;

    setWorks(prev => prev.map(w =>
      w.id === workId
        ? { ...w, isLiked: !wasLiked, likeCount: wasLiked ? oldCount - 1 : oldCount + 1 }
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
      setWorks(prev => prev.map(w =>
        w.id === workId ? { ...w, isLiked: wasLiked, likeCount: oldCount } : w
      ));
      message.error('操作失败');
    }
  };

  const handleFavorite = async (workId: number) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;

    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;

    setWorks(prev => prev.map(w =>
      w.id === workId
        ? { ...w, isFavorited: !wasFavorited, favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1 }
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
      setWorks(prev => prev.map(w =>
        w.id === workId ? { ...w, isFavorited: wasFavorited, favoriteCount: oldCount } : w
      ));
      message.error('操作失败');
    }
  };

  const handleClick = (workId: number) => {
    navigate(`/work/${workId}`);
  };

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 24 }}>
        <CompassOutlined style={{ color: '#667eea', fontSize: 20 }} />
        <Typography.Text strong style={{ fontSize: 18, color: '#1a1a2e' }}>推荐</Typography.Text>
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
                    onClick={handleClick}
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
          !loading && <Empty description="暂无推荐内容" />
        )}
      </Spin>
    </div>
  );
};

export default RecommendFeedPage;
