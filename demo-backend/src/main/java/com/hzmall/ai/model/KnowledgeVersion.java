package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeVersion {
    private Long id;
    private String versionName;
    private String description;
    private List<Long> includedFileIds;
    private int knowledgeCount;
    private String status; // draft, published, archived
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public KnowledgeVersion() {}

    public KnowledgeVersion(Long id, String versionName, String description, int knowledgeCount, String createdBy) {
        this.id = id;
        this.versionName = versionName;
        this.description = description;
        this.knowledgeCount = knowledgeCount;
        this.createdBy = createdBy;
        this.status = "draft";
        this.createdAt = LocalDateTime.now();
    }
}
