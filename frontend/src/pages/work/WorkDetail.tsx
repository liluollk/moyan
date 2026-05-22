import React, { useState, useEffect } from 'react';
import { Card, Avatar, Typography, Spin, Empty, Divider, Input, List, message, Image as AntImage } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import {
  HeartOutlined,
  HeartFilled,
  StarOutlined,
  StarFilled,
  ArrowLeftOutlined,
  PlusCircleFilled,
  CheckCircleFilled,
  MessageOutlined,
  UserOutlined,
  SendOutlined
} from '@ant-design/icons';
import { getWorkDetail } from '../../api/work';
import { likeWork, unlikeWork } from '../../api/like';
import { favoriteWork, unfavoriteWork } from '../../api/favorite';
import { followUser, unfollowUser } from '../../api/follow';
import { addComment, getComments } from '../../api/comment';
import { getCurrentUserInfo } from '../../api/auth';
import type { WorkVO, CommentVO } from '../../types';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

const WorkDetailPage: React.FC = () => {
  const { workId } = useParams<{ workId: string }>();
  const navigate = useNavigate();
  const [work, setWork] = useState<WorkVO | null>(null);
  const [loading, setLoading] = useState(false);
  const [comments, setComments] = useState<CommentVO[]>([]);
  const [commentContent, setCommentContent] = useState('');
  const [commentLoading, setCommentLoading] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<number | undefined>();

  const loadWorkDetail = async () => {
    if (!workId) return;
    setLoading(true);
    try {
      const response = await getWorkDetail(Number(workId));
      setWork(response.data.data);
    } catch (error: any) {
      message.error('加载作品详情失败');
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async () => {
    if (!workId) return;
    try {
      const response = await getComments(Number(workId), 1, 50);
      setComments(response.data.data.records || []);
    } catch {
      // comments load failure is non-critical
    }
  };

  useEffect(() => {
    loadWorkDetail();
    loadComments();
    getCurrentUserInfo().then(res => {
      if (res.data.code === 200) setCurrentUserId(res.data.data.id);
    }).catch(() => {});
  }, [workId]);

  const handleLike = async () => {
    if (!work) return;
    const wasLiked = work.isLiked;
    const oldCount = work.likeCount || 0;
    setWork(prev => prev ? { ...prev, isLiked: !wasLiked, likeCount: wasLiked ? oldCount - 1 : oldCount + 1 } : null);
    try {
      if (wasLiked) { await unlikeWork(work.id); message.success('已取消点赞'); }
      else { await likeWork(work.id); message.success('点赞成功'); }
    } catch (error: any) {
      setWork(prev => prev ? { ...prev, isLiked: wasLiked, likeCount: oldCount } : null);
      message.error('操作失败');
    }
  };

  const handleFavorite = async () => {
    if (!work) return;
    const wasFavorited = work.isFavorited;
    const oldCount = work.favoriteCount || 0;
    setWork(prev => prev ? { ...prev, isFavorited: !wasFavorited, favoriteCount: wasFavorited ? oldCount - 1 : oldCount + 1 } : null);
    try {
      if (wasFavorited) { await unfavoriteWork(work.id); message.success('已取消收藏'); }
      else { await favoriteWork(work.id); message.success('收藏成功'); }
    } catch (error: any) {
      setWork(prev => prev ? { ...prev, isFavorited: wasFavorited, favoriteCount: oldCount } : null);
      message.error('操作失败');
    }
  };

  const handleFollow = async () => {
    if (!work) return;
    const wasFollowing = work.isFollowing;
    setWork(prev => prev ? { ...prev, isFollowing: !wasFollowing } : null);
    try {
      if (wasFollowing) { await unfollowUser(work.userId); message.success('已取消关注'); }
      else { await followUser(work.userId); message.success('关注成功'); }
    } catch (error: any) {
      setWork(prev => prev ? { ...prev, isFollowing: wasFollowing } : null);
      message.error('操作失败');
    }
  };

  const handleAddComment = async () => {
    if (!work || !commentContent.trim()) { message.warning('请输入评论内容'); return; }
    setCommentLoading(true);
    try {
      const response = await addComment({ workId: work.id, content: commentContent.trim() });
      setComments(prev => [response.data.data, ...prev]);
      setCommentContent('');
      setWork(prev => prev ? { ...prev, commentCount: (prev.commentCount || 0) + 1 } : null);
      message.success('评论成功');
    } catch (error: any) {
      message.error('评论失败');
    } finally {
      setCommentLoading(false);
    }
  };

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  if (!work) return <Empty description="作品不存在" />;

  const ActionButton: React.FC<{
    icon: React.ReactNode;
    activeIcon: React.ReactNode;
    active?: boolean;
    label: string;
    count: number;
    color: string;
    onClick: () => void;
  }> = ({ icon, activeIcon, active, label, count, color, onClick }) => (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        padding: '10px 20px',
        borderRadius: 24,
        cursor: 'pointer',
        background: active ? `${color}12` : '#f7f8fa',
        transition: 'all 0.25s',
        userSelect: 'none',
      }}
    >
      <span style={{ fontSize: 18, color: active ? color : '#999', transition: 'all 0.25s', transform: active ? 'scale(1.2)' : 'scale(1)' }}>
        {active ? activeIcon : icon}
      </span>
      <Text style={{ fontSize: 14, fontWeight: 600, color: active ? color : '#666', margin: 0 }}>
        {count}
      </Text>
      <Text style={{ fontSize: 13, color: active ? color : '#999', margin: 0 }}>{label}</Text>
    </div>
  );

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      <div
        onClick={() => navigate(-1)}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 6,
          cursor: 'pointer',
          color: '#667eea',
          fontWeight: 500,
          fontSize: 14,
          marginBottom: 16,
          padding: '6px 14px',
          borderRadius: 10,
          transition: 'all 0.2s',
        }}
        onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(102,126,234,0.08)'}
        onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
      >
        <ArrowLeftOutlined /> 返回
      </div>

      <Card style={{ borderRadius: 20 }}>
        {/* User info */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Avatar src={work.avatar} size={48} icon={!work.avatar && <UserOutlined />}
              style={{ border: '2px solid white', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }} />
            <div style={{ marginLeft: 14 }}>
              <Text strong style={{ fontSize: 16 }}>{work.nickname}</Text>
              <Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: 2 }}>
                {dayjs(work.createTime).format('YYYY-MM-DD HH:mm')}
              </Text>
            </div>
          </div>
          {currentUserId && currentUserId !== work.userId && (
            <div
              onClick={handleFollow}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 6,
                padding: '8px 18px',
                borderRadius: 20,
                cursor: 'pointer',
                background: work.isFollowing
                  ? '#f7f8fa'
                  : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: work.isFollowing ? '#888' : 'white',
                fontWeight: 500,
                fontSize: 14,
                transition: 'all 0.25s',
                boxShadow: work.isFollowing ? 'none' : '0 4px 12px rgba(102,126,234,0.3)',
              }}
            >
              {work.isFollowing ? <CheckCircleFilled /> : <PlusCircleFilled />}
              {work.isFollowing ? '已关注' : '关注'}
            </div>
          )}
        </div>

        <Title level={3} style={{ marginBottom: 16, color: '#1a1a2e' }}>{work.title}</Title>

        {work.content && (
          <Paragraph style={{ fontSize: 15, lineHeight: 1.9, color: '#444', marginBottom: 24 }}>
            {work.content}
          </Paragraph>
        )}

        {work.images && work.images.length > 0 && (
          <div style={{ marginBottom: 24 }}>
            <AntImage.PreviewGroup>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {work.images.map((img, index) => (
                  <AntImage
                    key={index}
                    src={img}
                    alt={`图片${index + 1}`}
                    style={{
                      maxWidth: '100%',
                      maxHeight: 500,
                      objectFit: 'cover',
                      borderRadius: 12,
                    }}
                  />
                ))}
              </div>
            </AntImage.PreviewGroup>
          </div>
        )}

        <Divider style={{ margin: '0 0 20px' }} />

        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <ActionButton icon={<HeartOutlined />} activeIcon={<HeartFilled />} active={work.isLiked}
            label="点赞" count={work.likeCount || 0} color="#ff4d4f" onClick={handleLike} />
          <ActionButton icon={<StarOutlined />} activeIcon={<StarFilled />} active={work.isFavorited}
            label="收藏" count={work.favoriteCount || 0} color="#faad14" onClick={handleFavorite} />
          <ActionButton icon={<MessageOutlined />} activeIcon={<MessageOutlined />} active={false}
            label="评论" count={work.commentCount || 0} color="#667eea" onClick={() => {}} />
        </div>
      </Card>

      {/* 评论区域 */}
      <Card style={{ marginTop: 20, borderRadius: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20 }}>
          <MessageOutlined style={{ color: '#667eea', fontSize: 18 }} />
          <Text strong style={{ fontSize: 16 }}>评论 ({work.commentCount || 0})</Text>
        </div>

        <div style={{ display: 'flex', gap: 12, marginBottom: 20 }}>
          <TextArea
            rows={2}
            placeholder="写下你的评论..."
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            maxLength={1000}
            style={{ borderRadius: 14, flex: 1 }}
          />
          <div
            onClick={commentLoading ? undefined : handleAddComment}
            style={{
              alignSelf: 'flex-end',
              width: 44,
              height: 44,
              borderRadius: 14,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: commentContent.trim()
                ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                : '#f0f0f0',
              cursor: (commentLoading || !commentContent.trim()) ? 'not-allowed' : 'pointer',
              transition: 'all 0.25s',
              boxShadow: commentContent.trim() ? '0 4px 12px rgba(102,126,234,0.3)' : 'none',
              opacity: commentLoading ? 0.7 : 1,
            }}
          >
            {commentLoading
              ? <div style={{ width: 16, height: 16, border: '2px solid #fff', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.6s linear infinite' }} />
              : <SendOutlined style={{ color: commentContent.trim() ? 'white' : '#ccc', fontSize: 16 }} />
            }
          </div>
        </div>

        {comments.length > 0 ? (
          <List
            dataSource={comments}
            renderItem={(comment) => (
              <List.Item style={{ padding: '14px 0', borderBottom: '1px solid #f5f5f5' }}>
                <List.Item.Meta
                  avatar={<Avatar src={comment.avatar} icon={!comment.avatar && <UserOutlined />}
                    style={{ border: '2px solid white', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }} />}
                  title={
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Text strong style={{ fontSize: 14 }}>{comment.nickname}</Text>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {dayjs(comment.createTime).format('MM-DD HH:mm')}
                      </Text>
                    </div>
                  }
                  description={<Text style={{ color: '#555', lineHeight: 1.6, fontSize: 14 }}>{comment.content}</Text>}
                />
              </List.Item>
            )}
          />
        ) : (
          <Empty description="暂无评论，快来发表第一条评论吧" imageStyle={{ height: 80 }} />
        )}
      </Card>
    </div>
  );
};

export default WorkDetailPage;
