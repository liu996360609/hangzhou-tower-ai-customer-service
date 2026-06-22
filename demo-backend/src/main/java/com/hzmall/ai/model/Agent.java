package com.hzmall.ai.model;

import lombok.Data;

@Data
public class Agent {
    private Long id;
    private String name;
    private String role; // manager / agent
    private String status; // online / busy / offline
    private int currentSessions;
    private int maxSessions;
    private int totalResolved;
    private double satisfactionRate;

    public Agent() {}

    public Agent(Long id, String name, String role, String status, int currentSessions, int maxSessions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
        this.currentSessions = currentSessions;
        this.maxSessions = maxSessions;
        this.totalResolved = 0;
        this.satisfactionRate = 0.0;
    }
}
