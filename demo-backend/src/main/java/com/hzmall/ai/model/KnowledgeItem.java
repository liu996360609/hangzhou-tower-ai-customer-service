package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeItem {
    private Long id;
    private String question;
    private String answer;
    private String category;
    private List<String> tags;
    private String source;
    private int viewCount;
    private int likeCount;
    private String status; // draft, published, archived
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeItem() {}

    public KnowledgeItem(Long id, String question, String answer, String category, List<String> tags, String source) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.tags = tags;
        this.source = source;
        this.status = "published";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
