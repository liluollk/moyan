package com.liluo.moyan.module.ai.controller;

import com.liluo.moyan.module.ai.dto.AskRequest;
import com.liluo.moyan.module.ai.service.AiAskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Tag(name = "AI 智能问答")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiAskService aiAskService;

    @Operation(summary = "AI 流式问答（SSE）")
    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ask(@Valid @RequestBody AskRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);
        List<Map<String, String>> history = null;
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            history = request.getHistory().stream()
                .limit(10)
                .map(h -> Map.of("role", h.getRole(), "content", h.getContent()))
                .toList();
        }
        final var finalHistory = history;
        CompletableFuture.runAsync(() -> {
            try {
                aiAskService.askStreaming(request.getQuestion(), finalHistory, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}
