package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerServiceSession {
    private String id;
    private String userId;
    private String userName;
    private String userPhone;
    private Long agentId;
    private String agentName;
    private String status; // waiting, active, completed
    private int waitTimeSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    public CustomerServiceSession() {}

    public CustomerServiceSession(String id, String userId, String userName, String userPhone) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.status = "waiting";
        this.waitTimeSeconds = 0;
        this.createdAt = LocalDateTime.now();
    }
}
