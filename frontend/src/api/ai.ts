interface AskCallbacks {
  onSource: (source: 'rag' | 'fallback') => void;
  onToken: (token: string) => void;
  onError: (msg: string) => void;
  onDone: () => void;
}

export const askAiStreaming = async (
  question: string,
  history: Array<{ role: string; content: string }>,
  cb: AskCallbacks
) => {
  const token = localStorage.getItem('accessToken');
  const resp = await fetch('/api/ai/ask', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : '',
    },
    body: JSON.stringify({ question, history }),
  });

  if (!resp.ok) { cb.onError('请求失败'); return; }

  const reader = resp.body?.getReader();
  if (!reader) { cb.onError('浏览器不支持流式读取'); return; }

  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    const parts = buffer.split('\n\n');
    buffer = parts.pop() || '';

    for (const part of parts) {
      const lines = part.split('\n');
      let eventType = '';
      let dataStr = '';

      for (const line of lines) {
        if (line.startsWith('event:')) eventType = line.substring(6).trim();
        else if (line.startsWith('data:')) dataStr = line.substring(5).trim();
      }

      if (!dataStr) continue;

      if (eventType === 'source') {
        cb.onSource(dataStr === 'rag' ? 'rag' : 'fallback');
      } else if (eventType === 'token') {
        cb.onToken(dataStr);
      } else if (eventType === 'error') {
        try { cb.onError(JSON.parse(dataStr).msg || 'AI 服务错误'); } catch { cb.onError('AI 服务错误'); }
      }
    }
  }
  cb.onDone();
};
