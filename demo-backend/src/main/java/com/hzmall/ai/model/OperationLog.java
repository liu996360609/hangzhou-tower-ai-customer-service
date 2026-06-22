package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long id;
    private String operator;
    private String module;
    private String action;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;

    public OperationLog() {}

    public OperationLog(Long id, String operator, String module, String action, String description) {
        this.id = id;
        this.operator = operator;
        this.module = module;
        this.action = action;
        this.description = description;
        this.ipAddress = "192.168.1." + (int)(Math.random() * 255);
        this.createdAt = LocalDateTime.now();
    }
}
