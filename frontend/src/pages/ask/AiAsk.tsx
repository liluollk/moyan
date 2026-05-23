import React, { useState, useRef, useEffect } from 'react';
import { Card, Input, Typography, Spin, message } from 'antd';
import { SendOutlined, RobotOutlined, BulbOutlined } from '@ant-design/icons';
import { askAiStreaming } from '../../api/ai';

const { TextArea } = Input;
const { Title, Text } = Typography;

interface Message {
  type: 'user' | 'ai';
  content: string;
  source?: 'rag' | 'fallback';
  timestamp: number;
}

const STORAGE_KEY = 'moyan_ai_messages';

const loadMessages = (): Message[] => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch { return []; }
};

const AiAskPage: React.FC = () => {
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<Message[]>(loadMessages);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleAsk = () => {
    const q = question.trim();
    if (!q) return;
    if (q.length > 500) { message.warning('问题不能超过500个字符'); return; }

    const userMsg: Message = { type: 'user', content: q, timestamp: Date.now() };
    const aiMsg: Message = { type: 'ai', content: '', timestamp: Date.now() };

    // 取最近6条已完成的消息作为上下文
    const history = messages.slice(-6).map(m => ({
      role: m.type === 'user' ? 'user' : 'assistant',
      content: m.content,
    }));

    setMessages(prev => [...prev, userMsg, aiMsg]);
    setQuestion('');
    setLoading(true);

    askAiStreaming(q, history, {
      onSource: (source) => {
        setMessages(prev => {
          const updated = [...prev];
          const last = updated[updated.length - 1];
          if (last?.type === 'ai') {
            updated[updated.length - 1] = { ...last, source };
          }
          return updated;
        });
      },
      onToken: (token) => {
        setMessages(prev => {
          const updated = [...prev];
          const last = updated[updated.length - 1];
          if (last?.type === 'ai') {
            updated[updated.length - 1] = { ...last, content: last.content + token };
          }
          return updated;
        });
      },
      onError: (msg) => {
        message.error(msg);
        setLoading(false);
      },
      onDone: () => {
        setLoading(false);
      },
    });
  };

  const sampleQuestions = [
    'Spring Boot 有哪些特性？',
    '如何优化 MySQL 查询性能？',
    'Redis 缓存有哪些策略？',
    'React Hooks 怎么用？',
  ];

  return (
    <div style={{ maxWidth: 860, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <div style={{
          width: 44, height: 44, borderRadius: 14,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 4px 12px rgba(102,126,234,0.3)',
        }}>
          <RobotOutlined style={{ color: 'white', fontSize: 20 }} />
        </div>
        <div style={{ flex: 1 }}>
          <Title level={4} style={{ margin: 0, color: '#1a1a2e' }}>AI 智能问答</Title>
          <Text type="secondary" style={{ fontSize: 13 }}>基于社区作品内容，AI 为你解答创作相关问题</Text>
        </div>
        {messages.length > 0 && (
          <span
            onClick={() => { setMessages([]); localStorage.removeItem(STORAGE_KEY); }}
            style={{ fontSize: 12, color: '#bbb', cursor: 'pointer', userSelect: 'none' }}
          >
            清空对话
          </span>
        )}
      </div>

      {messages.length === 0 ? (
        <Card style={{ borderRadius: 20, textAlign: 'center', padding: '40px 0' }}>
          <div style={{
            width: 80, height: 80, borderRadius: 24, margin: '0 auto 24px',
            background: 'linear-gradient(135deg, rgba(102,126,234,0.1) 0%, rgba(118,75,162,0.1) 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <BulbOutlined style={{ fontSize: 36, color: '#667eea' }} />
          </div>
          <Title level={4} style={{ color: '#1a1a2e' }}>向社区 AI 助手提问</Title>
          <Text type="secondary" style={{ display: 'block', marginBottom: 24, fontSize: 14 }}>
            AI 会基于社区中的作品内容为你生成回答并标注参考来源
          </Text>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, justifyContent: 'center', maxWidth: 500, margin: '0 auto' }}>
            {sampleQuestions.map((q, i) => (
              <div
                key={i}
                onClick={() => setQuestion(q)}
                style={{
                  padding: '8px 18px', borderRadius: 20, cursor: 'pointer',
                  background: 'rgba(102,126,234,0.06)', color: '#667eea',
                  fontSize: 13, fontWeight: 500, border: '1px solid rgba(102,126,234,0.15)',
                  transition: 'all 0.2s',
                }}
                onMouseEnter={e => { e.currentTarget.style.background = 'rgba(102,126,234,0.12)'; }}
                onMouseLeave={e => { e.currentTarget.style.background = 'rgba(102,126,234,0.06)'; }}
              >
                {q}
              </div>
            ))}
          </div>
        </Card>
      ) : (
        <div style={{ marginBottom: 20 }}>
          {messages.map((msg, i) => (
            <div key={i} style={{ marginBottom: 16 }}>
              {msg.type === 'user' ? (
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <div style={{
                    maxWidth: '75%', padding: '12px 18px', borderRadius: 18,
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white', fontSize: 14, lineHeight: 1.6,
                  }}>
                    {msg.content}
                  </div>
                </div>
              ) : (
                <div>
                  {msg.source && (
                    <div style={{ marginBottom: 8 }}>
                      {msg.source === 'rag' ? (
                        <span style={{
                          fontSize: 11, padding: '2px 10px', borderRadius: 10,
                          background: 'rgba(82,196,26,0.1)', color: '#52c41a',
                          border: '1px solid rgba(82,196,26,0.2)',
                        }}>
                          基于社区内容
                        </span>
                      ) : (
                        <span style={{
                          fontSize: 11, padding: '2px 10px', borderRadius: 10,
                          background: 'rgba(250,173,20,0.1)', color: '#d48806',
                          border: '1px solid rgba(250,173,20,0.2)',
                        }}>
                          基于通用知识
                        </span>
                      )}
                    </div>
                  )}
                  <div style={{
                    padding: '16px 20px', borderRadius: 16,
                    background: '#f8f9fc', border: '1px solid #eef0f5',
                    minHeight: 24,
                  }}>
                    <div style={{ fontSize: 14, lineHeight: 1.8, color: '#333', whiteSpace: 'pre-wrap' }}>
                      {msg.content}
                      {loading && i === messages.length - 1 && !msg.content && (
                        <Spin size="small" />
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>
      )}

      {loading && messages.length > 0 && (
        <div style={{ textAlign: 'center', marginBottom: 12, fontSize: 12, color: '#999' }}>
          AI 正在书写...
        </div>
      )}

      <Card style={{ borderRadius: 20, position: 'sticky', bottom: 0 }}>
        <div style={{ display: 'flex', gap: 12 }}>
          <TextArea
            rows={2}
            placeholder="输入你的问题..."
            value={question}
            onChange={e => setQuestion(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleAsk();
              }
            }}
            maxLength={500}
            style={{ borderRadius: 14, flex: 1 }}
          />
          <div
            onClick={loading ? undefined : handleAsk}
            style={{
              alignSelf: 'flex-end', width: 44, height: 44, borderRadius: 14,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              background: question.trim() && !loading
                ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                : '#f0f0f0',
              cursor: (loading || !question.trim()) ? 'not-allowed' : 'pointer',
              transition: 'all 0.25s',
              boxShadow: question.trim() && !loading ? '0 4px 12px rgba(102,126,234,0.3)' : 'none',
              opacity: loading ? 0.7 : 1,
            }}
          >
            <SendOutlined style={{ color: question.trim() && !loading ? 'white' : '#ccc', fontSize: 16 }} />
          </div>
        </div>
      </Card>
    </div>
  );
};

export default AiAskPage;
