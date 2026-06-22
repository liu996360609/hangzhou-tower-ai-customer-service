package com.hzmall.ai.controller;

import com.hzmall.ai.model.*;
import com.hzmall.ai.service.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    // ==================== Role/Permission Management ====================

    @GetMapping("/roles")
    public ApiResponse<List<Role>> listRoles() {
        return ApiResponse.success(systemService.listRoles());
    }

    @GetMapping("/roles/{id}")
    public ApiResponse<Role> getRole(@PathVariable Long id) {
        Role role = systemService.getRole(id);
        return role != null ? ApiResponse.success(role) : ApiResponse.error(404, "角色不存在");
    }

    @PostMapping("/roles")
    public ApiResponse<Role> createRole(@RequestBody Role role) {
        return ApiResponse.success(systemService.createRole(role));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        Role updated = systemService.updateRole(id, role);
        return updated != null ? ApiResponse.success(updated) : ApiResponse.error(404, "角色不存在");
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        if (systemService.deleteRole(id)) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(404, "角色不存在");
    }

    // ==================== Operation Logs ====================

    @GetMapping("/logs")
    public ApiResponse<List<OperationLog>> listLogs(
            @RequestParam(required = false) String module) {
        return ApiResponse.success(systemService.listLogs(module));
    }

    // ==================== Sensitive Words ====================

    @GetMapping("/sensitive-words")
    public ApiResponse<List<SensitiveWord>> listSensitiveWords() {
        return ApiResponse.success(systemService.listSensitiveWords());
    }

    @PostMapping("/sensitive-words")
    public ApiResponse<SensitiveWord> createSensitiveWord(@RequestBody SensitiveWord word) {
        return ApiResponse.success(systemService.createSensitiveWord(word));
    }

    @PutMapping("/sensitive-words/{id}")
    public ApiResponse<SensitiveWord> updateSensitiveWord(@PathVariable Long id,
                                                            @RequestBody SensitiveWord word) {
        SensitiveWord updated = systemService.updateSensitiveWord(id, word);
        return updated != null ? ApiResponse.success(updated) : ApiResponse.error(404, "敏感词不存在");
    }

    @DeleteMapping("/sensitive-words/{id}")
    public ApiResponse<Void> deleteSensitiveWord(@PathVariable Long id) {
        if (systemService.deleteSensitiveWord(id)) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(404, "敏感词不存在");
    }
}
