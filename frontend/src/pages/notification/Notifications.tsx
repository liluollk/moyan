import React, { useState, useEffect } from 'react';
import { List, Card, Badge, Empty, Spin, Typography } from 'antd';
import { BellOutlined, HeartFilled, StarFilled, MessageFilled, TeamOutlined } from '@ant-design/icons';
import { getNotificationList } from '../../api/notification';
import type { Notification } from '../../types';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const { Text } = Typography;

const typeConfig: Record<number, { icon: React.ReactNode; color: string; label: string }> = {
  1: { icon: <HeartFilled />, color: '#ff4d4f', label: '点赞' },
  2: { icon: <StarFilled />, color: '#faad14', label: '收藏' },
  3: { icon: <MessageFilled />, color: '#667eea', label: '评论' },
  4: { icon: <TeamOutlined />, color: '#52c41a', label: '关注' },
};

const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const response = await getNotificationList(1, 50);
      setNotifications(response.data.data.records);
    } catch {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadNotifications(); }, []);

  return (
    <div style={{ maxWidth: 1060, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <div style={{
          width: 44,
          height: 44,
          borderRadius: 14,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 4px 12px rgba(102,126,234,0.3)',
        }}>
          <BellOutlined style={{ color: 'white', fontSize: 20 }} />
        </div>
        <div>
          <Typography.Title level={4} style={{ margin: 0, color: '#1a1a2e' }}>通知中心</Typography.Title>
          <Text type="secondary" style={{ fontSize: 13 }}>查看你的最新消息</Text>
        </div>
      </div>

      <Card style={{ borderRadius: 20 }}>
        <Spin spinning={loading}>
          {notifications.length > 0 ? (
            <List
              dataSource={notifications}
              renderItem={(item) => {
                const config = typeConfig[item.type] || { icon: <BellOutlined />, color: '#667eea', label: '通知' };
                return (
                  <List.Item style={{
                    padding: '16px',
                    borderRadius: 14,
                    marginBottom: 8,
                    background: item.isRead === 0 ? 'rgba(102,126,234,0.04)' : 'transparent',
                    border: item.isRead === 0 ? '1px solid rgba(102,126,234,0.1)' : '1px solid transparent',
                    transition: 'all 0.2s',
                  }}>
                    <List.Item.Meta
                      avatar={
                        <Badge dot={item.isRead === 0} offset={[-4, 4]}>
                          <div style={{
                            width: 44,
                            height: 44,
                            borderRadius: 14,
                            background: `${config.color}14`,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: 18,
                            color: config.color,
                          }}>
                            {config.icon}
                          </div>
                        </Badge>
                      }
                      title={
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Text strong style={{ color: config.color, fontSize: 14 }}>
                            {config.label}
                          </Text>
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            {dayjs(item.createTime).fromNow()}
                          </Text>
                        </div>
                      }
                      description={
                        <Text style={{ color: '#555', fontSize: 14, lineHeight: 1.6 }}>
                          {item.content}
                        </Text>
                      }
                    />
                  </List.Item>
                );
              }}
            />
          ) : (
            !loading && <Empty description="暂无通知" imageStyle={{ height: 80 }} />
          )}
        </Spin>
      </Card>
    </div>
  );
};

export default NotificationsPage;
