package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SensitiveWord {
    private Long id;
    private String word;
    private String category;
    private String action; // block / replace
    private String replacement;
    private boolean enabled;
    private LocalDateTime createdAt;

    public SensitiveWord() {}

    public SensitiveWord(Long id, String word, String category, String action, String replacement) {
        this.id = id;
        this.word = word;
        this.category = category;
        this.action = action;
        this.replacement = replacement;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
    }
}
