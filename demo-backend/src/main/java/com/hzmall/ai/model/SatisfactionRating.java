package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SatisfactionRating {
    private Long id;
    private String sessionId;
    private int score; // 1-5
    private String comment;
    private LocalDateTime createdAt;

    public SatisfactionRating() {}

    public SatisfactionRating(Long id, String sessionId, int score, String comment) {
        this.id = id;
        this.sessionId = sessionId;
        this.score = score;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
}
