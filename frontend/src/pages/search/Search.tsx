import React, { useState, useEffect } from 'react';
import { Row, Col, Button, Empty, Spin, Typography } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { searchWorks } from '../../api/search';
import { likeWork, unlikeWork } from '../../api/like';
import { favoriteWork, unfavoriteWork } from '../../api/favorite';
import WorkCard from '../../components/work/WorkCard';
import type { WorkVO } from '../../types';
import { message } from 'antd';

const { Text } = Typography;

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [works, setWorks] = useState<WorkVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const handleSearch = async () => {
    if (!keyword.trim()) { message.warning('请输入搜索关键词'); return; }
    setLoading(true);
    setSearched(true);
    try {
      const response = await searchWorks(keyword.trim(), 0, 50);
      setWorks(response.data.data);
    } catch (error: any) {
      message.error('搜索失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const kw = searchParams.get('keyword');
    if (kw) { setKeyword(kw); }
  }, [searchParams]);

  useEffect(() => {
    if (keyword.trim()) handleSearch();
  }, [keyword]);

  const handleLike = async (workId: string) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;
    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;
    setWorks(prev => prev.map(w => w.id === workId ? { ...w, isLiked: !wasLiked, likeCount: wasLiked ? oldCount - 1 : oldCount + 1 } : w));
    try {
      if (wasLiked) { await unlikeWork(workId); message.success('已取消点赞'); }
      else { await likeWork(workId); message.success('点赞成功'); }
    } catch (error: any) {
      setWorks(prev => prev.map(w => w.id === workId ? { ...w, isLiked: wasLiked, likeCount: oldCount } : w));
      message.error('操作失败');
    }
  };

  const handleFavorite = async (workId: string) => {
    const work = works.find(w => w.id === workId);
    if (!work) return;
    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;
    setWorks(prev => prev.map(w => w.id === workId ? { ...w, isFavorited: !wasFavorited, favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1 } : w));
    try {
      if (wasFavorited) { await unfavoriteWork(workId); message.success('已取消收藏'); }
      else { await favoriteWork(workId); message.success('收藏成功'); }
    } catch (error: any) {
      setWorks(prev => prev.map(w => w.id === workId ? { ...w, isFavorited: wasFavorited, favoriteCount: oldCount } : w));
      message.error('操作失败');
    }
  };

  const handleClick = (workId: string) => navigate(`/work/${workId}`);

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      {/* Search header */}
      <div style={{
        display: 'flex',
        gap: 12,
        marginBottom: 28,
      }}>
        <div style={{
          flex: 1,
          position: 'relative',
        }}>
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') handleSearch(); }}
            placeholder="搜索创意作品..."
            style={{
              width: '100%',
              height: 50,
              border: '2px solid #e8e8e8',
              borderRadius: 16,
              padding: '0 20px 0 48px',
              fontSize: 15,
              outline: 'none',
              background: 'rgba(255,255,255,0.95)',
              transition: 'all 0.3s',
              backdropFilter: 'blur(10px)',
            }}
            onFocus={(e) => { e.target.style.borderColor = '#667eea'; e.target.style.boxShadow = '0 0 0 4px rgba(102,126,234,0.1)'; }}
            onBlur={(e) => { e.target.style.borderColor = '#e8e8e8'; e.target.style.boxShadow = 'none'; }}
          />
          <SearchOutlined style={{
            position: 'absolute',
            left: 18,
            top: '50%',
            transform: 'translateY(-50%)',
            color: '#bbb',
            fontSize: 16,
          }} />
        </div>
        <Button
          type="primary"
          icon={<SearchOutlined />}
          onClick={handleSearch}
          style={{
            height: 50,
            borderRadius: 16,
            padding: '0 28px',
            fontSize: 15,
            fontWeight: 600,
          }}
        >
          搜索
        </Button>
      </div>

      <Spin spinning={loading}>
        {searched && works.length > 0 ? (
          <>
            <Text type="secondary" style={{ display: 'block', marginBottom: 16, fontSize: 13 }}>
              找到 <Text strong style={{ color: '#667eea' }}>{works.length}</Text> 个结果
            </Text>
            <Row gutter={[16, 16]}>
              {works.map((work) => (
                <Col span={24} key={work.id}>
                  <WorkCard work={work} onLike={handleLike} onFavorite={handleFavorite} onClick={handleClick} />
                </Col>
              ))}
            </Row>
          </>
        ) : (
          searched && !loading && <Empty description="未找到相关作品" imageStyle={{ height: 100 }} />
        )}
        {!searched && !loading && (
          <Empty description="输入关键词搜索创意作品" imageStyle={{ height: 100 }} />
        )}
      </Spin>
    </div>
  );
};

export default SearchPage;
