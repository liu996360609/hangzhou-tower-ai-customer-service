package com.hzmall.ai.model;

import lombok.Data;

@Data
public class Role {
    private Long id;
    private String name;
    private String code;
    private String description;
    private java.util.List<String> permissions;
    private int userCount;

    public Role() {}

    public Role(Long id, String name, String code, String description, java.util.List<String> permissions) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.permissions = permissions;
        this.userCount = 0;
    }
}
