package com.liluo.moyan.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AskRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题不能超过500个字符")
    private String question;

    private List<HistoryItem> history;

    @Data
    public static class HistoryItem {
        private String role;  // "user" | "assistant"
        private String content;
    }
}
