package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String id;
    private String sessionId;
    private String role; // user / assistant / system
    private String content;
    private LocalDateTime createdAt;

    public ChatMessage() {}

    public ChatMessage(String id, String sessionId, String role, String content) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
