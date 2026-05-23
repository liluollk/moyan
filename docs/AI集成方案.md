# 墨言 AI 集成方案

## 一、功能规划

### 1. 作品润色/AI 改写（第一期）
用户在 Markdown 编辑框写完内容后，一键 AI 润色或扩写。

支持三种模式：
- **润色 polish**：修正错别字和语法、优化表达流畅度、合理分段
- **扩写 expand**：展开核心观点、增加细节和例证
- **摘要 summarize**：提取核心要点

### 2. 语义搜索（第二期）
用向量检索替代纯关键词匹配：文本 Embedding → 向量数据库（Milvus/pgvector）→ 语义相似度检索，ES 高亮结果 + 向量搜索排序。

### 3. 智能标签/分类（第二期）
AI 自动分析作品内容生成标签，免去用户手动选择。

### 4. AI 内容审核（第三期）
自动检测违规图片和敏感文字，社区上线必备。

### 5. 个性化推荐（第三期）
分析用户行为（点赞/收藏/浏览时长），训练推荐模型优化 Feed 流排序。

---

## 二、LLM 接入选型

| 模型 | 接口格式 | 特点 |
|------|----------|------|
| DeepSeek | OpenAI 兼容 | 中文写作好，极便宜 |
| 通义千问 Qwen | OpenAI 兼容 | 中文理解好，便宜 |
| 智谱 GLM | OpenAI 兼容 | 老牌国产 |

推荐 **DeepSeek** 或 **千问**。三家的 API 都是 OpenAI 兼容格式，代码写一套，换模型只改 URL 和 Key。

调用方式：统一走 OpenAI 兼容接口

```
POST https://api.deepseek.com/v1/chat/completions
Authorization: Bearer <API_KEY>
Body: {
  "model": "deepseek-chat",
  "messages": [
    { "role": "system", "content": "你是专业的文字润色助手..." },
    { "role": "user", "content": "请润色以下内容：\n{用户原文}" }
  ],
  "temperature": 0.7,
  "max_tokens": 2000
}
```

---

## 三、后端设计

### API 端点

```
POST /api/ai/polish
Request:  { "content": "用户原文", "mode": "polish" | "expand" | "summarize" }
Response: { "polishedContent": "润色后文本" }
```

### 关键实现

- **超时控制**：LLM 调用设 30s 超时
- **配置外置**：API Key 通过环境变量注入，不硬编码
- **失败降级**：LLM 调用失败返回原文 + 错误提示，不阻塞用户发布
- **字数限制**：原文超 5000 字截断，防 token 消耗过大

### 新增文件

```
src/main/java/com/liluo/moyan/module/ai/
  controller/AiController.java    # POST /api/ai/polish
  dto/PolishRequest.java          # { content, mode }
  service/AiPolishService.java    # LLM API 调用 + 降级 + 缓存
```

配置（application-example.yml）：

```yaml
ai:
  enabled: true
  provider: deepseek              # deepseek | qwen | glm
  api-key: ${AI_API_KEY:}
  polish:
    model: deepseek-chat
    max-tokens: 2000
    timeout: 30s
```

---

## 四、Prompt 设计

### 润色模式
```
你是一个专业的中文写作助手。请润色用户提供的文本，要求：
- 修正错别字和语法错误
- 优化句子流畅度和表达
- 保持原文的意思和风格不变
- 合理分段，增加可读性
- 直接返回润色后的文本，不要解释修改了哪里
```

### 扩写模式
```
你是一个专业的中文写作助手。请基于用户提供的主题或草稿进行扩写，要求：
- 展开核心观点，增加细节和例证
- 保持逻辑清晰，段落分明
- 语气自然，不过度华丽
- 直接返回扩写后的文本
```

### 摘要模式
```
你是一个专业的中文写作助手。请对用户提供的文本生成摘要，要求：
- 提取核心要点，控制原文字数的 20% 以内
- 简洁明了，层次清晰
- 直接返回摘要，不要额外说明
```

---

## 五、前端交互

### Publish.tsx 改动

编辑区顶部工具栏增加 AI 按钮：

```
[图片上传] [Markdown 预览] [AI 润色 ▼]
                             ├─ 润色
                             ├─ 扩写
                             └─ 摘要
```

### 交互流程

1. 用户点击「润色」→ 按钮显示 loading 状态
2. 后端返回后 → 弹出**对比弹窗**（左原文 / 右润色结果）
3. 用户选择「替换」或「取消」
4. 替换后仍可手动编辑

### 对比弹窗设计

- Modal 宽度 900px，左右对称两栏
- 左栏标题「原文」，右栏标题「润色结果」
- 右栏高亮显示变更部分
- 底部按钮：取消 / 替换内容

### 状态处理

| 状态 | 表现 |
|------|------|
| 请求中 | 按钮 loading + 文本「AI 正在润色...」 |
| 成功 | 对比弹窗 |
| 超时/失败 | message.error「AI 服务繁忙，请稍后重试」 |
| 原文过短 < 20字 | message.warning「内容太短，不需要润色」 |

### 新增/修改文件

```
frontend/src/
  api/ai.ts              # 新增：polishContent API 封装
  pages/work/Publish.tsx  # 修改：加 AI 按钮 + 对比弹窗
```

---

## 六、成本控制

| 措施 | 说明 |
|------|------|
| 频次限制 | 同一用户每分钟最多 5 次 |
| 内容去重 | 相同原文 + 相同模式 5 分钟内返回缓存 |
| Token 上限 | 单次请求总量 max 4000 token |
| 全局开关 | `ai.enabled: false` 一键关闭 |
| 模式切换 | 可独立控制每种模式开关 |

---

## 七、后续扩展方向

- 关键词生成：AI 自动提取作品关键词存入 ES，增强搜索召回
- 自动摘要：作品列表/Feed 流用 AI 摘要替代内容截断
- 评论区 AI 助手：一键生成有质量的评论
- 智能回复建议：作者收到评论后 AI 辅助回复
