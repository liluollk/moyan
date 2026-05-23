import React, { useState } from 'react';
import { Form, Input, Button, Card, Upload, message, Typography } from 'antd';
import { LoadingOutlined, EditOutlined, PictureOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { publishWork } from '../../api/work';
import { uploadFile } from '../../api/file';
import type { PublishWorkRequest } from '../../types';
import type { UploadFile } from 'antd/es/upload/interface';

const { TextArea } = Input;
const { Title, Text } = Typography;

const PublishPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (file: File) => {
    setUploading(true);
    try {
      const response = await uploadFile(file);
      const url = response.data.data;
      const newFile: UploadFile = {
        uid: Date.now().toString(),
        name: file.name,
        status: 'done',
        url: url
      };
      setFileList(prev => [...prev, newFile]);
      message.success('上传成功');
    } catch (error: any) {
      message.error('上传失败');
    } finally {
      setUploading(false);
    }
    return false;
  };

  const onFinish = async (values: PublishWorkRequest) => {
    setLoading(true);
    try {
      const images = fileList.map(file => file.url).filter(Boolean) as string[];
      await publishWork({ ...values, images: images.length > 0 ? images : undefined });
      message.success('发布成功');
      navigate('/');
    } catch (error: any) {
      message.error(error.response?.data?.message || '发布失败');
    } finally {
      setLoading(false);
    }
  };

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
          <EditOutlined style={{ color: 'white', fontSize: 20 }} />
        </div>
        <div>
          <Title level={4} style={{ margin: 0, color: '#1a1a2e' }}>发布作品</Title>
          <Text type="secondary" style={{ fontSize: 13 }}>分享你的创意与灵感</Text>
        </div>
      </div>

      <Card style={{ borderRadius: 20 }}>
        <Form name="publish" onFinish={onFinish} layout="vertical" size="large">
          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入标题' }, { max: 200, message: '标题不能超过200个字符' }]}
          >
            <Input
              placeholder="请输入作品标题"
              style={{ height: 48, borderRadius: 12 }}
              prefix={<EditOutlined style={{ color: '#bbb', fontSize: 14 }} />}
            />
          </Form.Item>

          <Form.Item name="content" label="内容">
            <TextArea
              rows={6}
              placeholder="请输入作品内容（可选）"
              showCount
              maxLength={2000}
              style={{ borderRadius: 12, fontSize: 14, lineHeight: 1.8 }}
            />
          </Form.Item>

          <Form.Item label="图片">
            <Upload
              listType="picture-card"
              fileList={fileList}
              customRequest={({ file }) => handleUpload(file as File)}
              onRemove={(file) => setFileList(prev => prev.filter(item => item.uid !== file.uid))}
            >
              {fileList.length >= 9 ? null : (
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
                  {uploading ? <LoadingOutlined style={{ fontSize: 20, color: '#667eea' }} /> :
                    <PictureOutlined style={{ fontSize: 20, color: '#667eea' }} />}
                  <Text type="secondary" style={{ fontSize: 12 }}>上传图片</Text>
                </div>
              )}
            </Upload>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              size="large"
              block
              style={{ height: 50, borderRadius: 14, fontSize: 16, fontWeight: 600 }}
            >
              发布作品
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default PublishPage;
