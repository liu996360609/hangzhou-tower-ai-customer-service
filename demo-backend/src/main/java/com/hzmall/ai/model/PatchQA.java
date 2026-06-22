package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PatchQA {
    private Long id;
    private String question;
    private String answer;
    private String triggerKeywords;
    private int hitCount;
    private boolean enabled;
    private int priority; // higher = higher priority
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PatchQA() {}

    public PatchQA(Long id, String question, String answer, String triggerKeywords, int priority) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.triggerKeywords = triggerKeywords;
        this.hitCount = 0;
        this.enabled = true;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
