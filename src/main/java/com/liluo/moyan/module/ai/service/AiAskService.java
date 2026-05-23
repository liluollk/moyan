package com.liluo.moyan.module.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liluo.moyan.module.search.service.SearchService;
import com.liluo.moyan.module.work.vo.WorkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiAskService {

    @Autowired
    private SearchService searchService;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void askStreaming(String question, List<Map<String, String>> history, SseEmitter emitter) {
        if (apiKey == null || apiKey.isBlank()) {
            sendSse(emitter, "error", "{\"msg\":\"AI Key 未配置\"}");
            emitter.complete();
            return;
        }

        // 1. 检索候选作品
        List<WorkVO> candidates = searchService.searchWorks(question, 0, 5).stream()
            .filter(w -> w.getContent() != null && !w.getContent().isBlank())
            .limit(3)
            .collect(Collectors.toList());

        // 2. 构建 Prompt — LLM 自己判断相关性，代码不做预判
        StringBuilder ctx = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            WorkVO w = candidates.get(i);
            ctx.append("【").append(i + 1).append("】《").append(w.getTitle()).append("》\n")
                .append(w.getContent()).append("\n\n");
        }

        boolean hasCandidates = !ctx.isEmpty();

        String systemPrompt = hasCandidates ? """
            你是墨言创意社区的 AI 助手。下方是社区候选作品，请优先使用它们回答问题，
            提到作品标题。只有当所有作品都确实与问题毫不相关时，
            才用自己的知识回答（此时开头说"社区暂无相关内容"）。
            """: """
            你是墨言创意社区的 AI 助手。请直接基于知识回答用户问题。
            回答末尾加一句"（基于通用知识，社区暂无相关内容）"。
            """;

        String userMsg = hasCandidates
            ? "候选作品：\n\n" + ctx + "问题：" + question
            : "问题：" + question;

        // 3. 发送来源标记
        sendSse(emitter, "source", hasCandidates ? "rag" : "fallback");

        // 4. 构建消息 + 调用 LLM
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) messages.addAll(history);
        messages.add(Map.of("role", "user", "content", userMsg));

        try {
            callStreamingLLM(messages, emitter);
        } catch (Exception e) {
            log.error("流式 LLM 调用失败", e);
            sendSse(emitter, "error", "{\"msg\":\"AI 服务繁忙\"}");
        }
        emitter.complete();
    }

    private void callStreamingLLM(List<Map<String, String>> messages, SseEmitter emitter) throws IOException {
        URI uri = URI.create(baseUrl + "/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        String body = objectMapper.writeValueAsString(Map.of(
            "model", model,
            "messages", messages,
            "temperature", 0.7,
            "max_tokens", 2000,
            "stream", true
        ));

        conn.getOutputStream().write(body.getBytes());
        conn.getOutputStream().close();

        if (conn.getResponseCode() != 200) {
            log.error("LLM API 返回 {}", conn.getResponseCode());
            sendSse(emitter, "error", "{\"msg\":\"AI 服务返回错误\"}");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) break;
                    try {
                        JsonNode choice = objectMapper.readTree(data).path("choices").get(0);
                        JsonNode delta = choice.path("delta");
                        if (delta.has("content")) {
                            sendSse(emitter, "token", delta.path("content").asText());
                        }
                    } catch (Exception e) {
                        log.debug("解析流式数据失败: {}", data);
                    }
                }
            }
        }
    }

    private void sendSse(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException e) {
            log.debug("SSE 发送失败: {}", e.getMessage());
        }
    }
}
