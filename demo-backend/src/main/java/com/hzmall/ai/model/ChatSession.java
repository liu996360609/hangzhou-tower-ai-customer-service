package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatSession {
    private String id;
    private String userId;
    private String userName;
    private String status; // active, transferred, closed
    private List<ChatMessage> messages;
    private boolean transferredToHuman;
    private String transferReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChatSession() {}

    public ChatSession(String id, String userId, String userName) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.status = "active";
        this.transferredToHuman = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
