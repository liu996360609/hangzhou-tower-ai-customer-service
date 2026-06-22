package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeFile {
    private Long id;
    private Long categoryId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String uploadStatus; // uploading, parsing, parsed, failed
    private int parseProgress;
    private String parseError;
    private int chunkCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime parsedAt;

    public KnowledgeFile() {}

    public KnowledgeFile(Long id, Long categoryId, String fileName, String fileType, long fileSize,
                         String uploadStatus, int parseProgress, int chunkCount) {
        this.id = id;
        this.categoryId = categoryId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadStatus = uploadStatus;
        this.parseProgress = parseProgress;
        this.chunkCount = chunkCount;
        this.uploadedAt = LocalDateTime.now();
    }
}
