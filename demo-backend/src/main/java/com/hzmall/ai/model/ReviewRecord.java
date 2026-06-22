package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewRecord {
    private Long id;
    private Long fileId;
    private String fileName;
    private String status; // pending_review_a, pending_review_b, published, rejected
    private String reviewABy;
    private String reviewAAt;
    private String reviewAComment;
    private String reviewBBy;
    private String reviewBAt;
    private String reviewBComment;
    private String rejectReason;
    private LocalDateTime createdAt;

    public ReviewRecord() {}

    public ReviewRecord(Long id, Long fileId, String fileName, String status) {
        this.id = id;
        this.fileId = fileId;
        this.fileName = fileName;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
