import React from 'react';
import { Card, Avatar, Typography } from 'antd';
import {
  HeartOutlined,
  HeartFilled,
  StarOutlined,
  StarFilled,
  MessageOutlined,
  UserOutlined,
  PlusCircleFilled,
  CheckCircleFilled
} from '@ant-design/icons';
import type { WorkVO } from '../../types';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const { Text } = Typography;

interface WorkCardProps {
  work: WorkVO;
  onLike?: (workId: string) => void;
  onFavorite?: (workId: string) => void;
  onFollow?: (userId: string) => void;
  onClick?: (workId: string) => void;
  currentUserId?: string;
}

const ActionBtn: React.FC<{
  icon: React.ReactNode;
  activeIcon?: React.ReactNode;
  active?: boolean;
  count: number;
  activeColor?: string;
  onClick?: (e: React.MouseEvent) => void;
}> = ({ icon, activeIcon, active, count, activeColor, onClick }) => (
  <div
    onClick={onClick}
    style={{
      display: 'flex',
      alignItems: 'center',
      gap: 6,
      padding: '6px 14px',
      borderRadius: 20,
      cursor: 'pointer',
      background: active ? `${activeColor}14` : '#f7f8fa',
      transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
      userSelect: 'none',
    }}
    onMouseEnter={(e) => {
      if (!active) e.currentTarget.style.background = '#f0f1f5';
    }}
    onMouseLeave={(e) => {
      if (!active) e.currentTarget.style.background = '#f7f8fa';
    }}
  >
    <span style={{
      fontSize: 16,
      color: active ? activeColor : '#999',
      transition: 'all 0.25s',
      transform: active ? 'scale(1.15)' : 'scale(1)',
    }}>
      {active && activeIcon ? activeIcon : icon}
    </span>
    <Text style={{
      fontSize: 13,
      fontWeight: 500,
      color: active ? activeColor : '#888',
      margin: 0,
    }}>
      {count}
    </Text>
  </div>
);

const stripHtml = (html: string) => html.replace(/<[^>]*>/g, '');

const truncateText = (text: string, maxLen: number): string => {
  if (stripHtml(text).length <= maxLen) return text;
  let result = '';
  let plainLen = 0;
  let inTag = false;
  for (const ch of text) {
    if (ch === '<') inTag = true;
    if (!inTag) plainLen++;
    result += ch;
    if (ch === '>') inTag = false;
    if (plainLen >= maxLen && !inTag) break;
  }
  return result + '...';
};

const HighlightText: React.FC<{ text: string; className?: string; style?: React.CSSProperties }> = ({ text, className, style }) => {
  if (text.includes('<em>')) {
    return <span className={className} style={style} dangerouslySetInnerHTML={{ __html: text }} />;
  }
  return <span className={className} style={style}>{text}</span>;
};

const WorkCard: React.FC<WorkCardProps> = ({ work, onLike, onFavorite, onFollow, onClick, currentUserId }) => {
  const isOwnWork = currentUserId === work.userId;

  return (
    <Card
      hoverable
      style={{
        marginBottom: 20,
        borderRadius: 20,
        overflow: 'hidden',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        border: 'none',
      }}
      bodyStyle={{ padding: 0 }}
      onClick={() => onClick?.(work.id)}
      cover={
        work.images && work.images.length > 0 ? (
          <div style={{
            maxHeight: 420,
            overflow: 'hidden',
            position: 'relative',
          }}>
            <img
              src={work.images[0]}
              alt={work.title}
              style={{
                width: '100%',
                height: 420,
                objectFit: 'cover',
                transition: 'transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'scale(1.03)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'scale(1)';
              }}
            />
            <div style={{
              position: 'absolute',
              bottom: 0,
              left: 0,
              right: 0,
              background: 'linear-gradient(to top, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0.2) 60%, transparent 100%)',
              padding: '60px 24px 20px',
            }}>
              <HighlightText text={work.title} style={{
                color: 'white',
                fontSize: 20,
                fontWeight: 700,
                textShadow: '0 2px 8px rgba(0,0,0,0.4)',
                display: 'block',
              }} />
            </div>
          </div>
        ) : (
          <div style={{
            background: 'linear-gradient(135deg, #667eea11 0%, #764ba211 100%)',
            padding: '32px 24px',
            borderBottom: '1px solid #f0f0f0',
          }}>
            <HighlightText text={work.title} style={{
              color: '#1a1a2e',
              fontSize: 18,
              fontWeight: 700,
              display: 'block',
            }} />
          </div>
        )
      }
    >
      {/* User info + content */}
      <div style={{ padding: '16px 24px 20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 12 }}>
          <div style={{ position: 'relative', flexShrink: 0 }}>
            <Avatar
              size={44}
              src={work.avatar}
              icon={!work.avatar && <UserOutlined />}
              style={{
                border: '3px solid white',
                boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
              }}
            />
            {!isOwnWork && onFollow && (
              <div
                onClick={(e) => {
                  e.stopPropagation();
                  onFollow(work.userId);
                }}
                style={{
                  position: 'absolute',
                  bottom: -4,
                  right: -4,
                  width: 22,
                  height: 22,
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: 'pointer',
                  border: '2.5px solid white',
                  background: work.isFollowing
                    ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                    : 'linear-gradient(135deg, #52c41a 0%, #389e0d 100%)',
                  boxShadow: '0 2px 6px rgba(0,0,0,0.15)',
                  transition: 'all 0.2s',
                }}
              >
                {work.isFollowing
                  ? <CheckCircleFilled style={{ color: 'white', fontSize: 11 }} />
                  : <PlusCircleFilled style={{ color: 'white', fontSize: 11 }} />
                }
              </div>
            )}
          </div>
          <div style={{ marginLeft: 12, flex: 1, minWidth: 0 }}>
            <Text strong style={{ fontSize: 15, color: '#1a1a2e' }}>
              {work.nickname}
            </Text>
            <Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: 2 }}>
              {dayjs(work.createTime).fromNow()}
            </Text>
          </div>
        </div>

        {work.content && (
          <Text style={{
            display: 'block',
            marginBottom: 16,
            lineHeight: 1.7,
            color: '#555',
            fontSize: 14,
          }}>
            <HighlightText text={truncateText(work.content, 150)} />
          </Text>
        )}

        {/* Action buttons */}
        <div style={{ display: 'flex', gap: 8 }}>
          <ActionBtn
            icon={<HeartOutlined />}
            activeIcon={<HeartFilled />}
            active={work.isLiked}
            count={work.likeCount || 0}
            activeColor="#ff4d4f"
            onClick={(e) => { e.stopPropagation(); onLike?.(work.id); }}
          />
          <ActionBtn
            icon={<StarOutlined />}
            activeIcon={<StarFilled />}
            active={work.isFavorited}
            count={work.favoriteCount || 0}
            activeColor="#faad14"
            onClick={(e) => { e.stopPropagation(); onFavorite?.(work.id); }}
          />
          <ActionBtn
            icon={<MessageOutlined />}
            count={work.commentCount || 0}
            activeColor="#667eea"
          />
        </div>
      </div>
    </Card>
  );
};

export default WorkCard;
