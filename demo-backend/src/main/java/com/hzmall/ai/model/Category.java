package com.hzmall.ai.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private String code;
    private String description;
    private int sortOrder;
    private int fileCount;
    private String status; // active / inactive
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Category() {}

    public Category(Long id, String name, String code, String description, int sortOrder, int fileCount, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.sortOrder = sortOrder;
        this.fileCount = fileCount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
