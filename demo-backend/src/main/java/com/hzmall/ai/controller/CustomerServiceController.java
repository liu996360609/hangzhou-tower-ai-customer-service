package com.hzmall.ai.controller;

import com.hzmall.ai.model.*;
import com.hzmall.ai.service.CustomerServiceAgentService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/cs")
public class CustomerServiceController {

    private final CustomerServiceAgentService agentService;

    public CustomerServiceController(CustomerServiceAgentService agentService) {
        this.agentService = agentService;
    }

    // ==================== Agent Management ====================

    @GetMapping("/agents")
    public ApiResponse<List<Agent>> listAgents() {
        return ApiResponse.success(agentService.listAgents());
    }

    @GetMapping("/agents/{id}")
    public ApiResponse<Agent> getAgent(@PathVariable Long id) {
        Agent a = agentService.getAgent(id);
        return a != null ? ApiResponse.success(a) : ApiResponse.error(404, "坐席不存在");
    }

    @PutMapping("/agents/{id}/status")
    public ApiResponse<Agent> updateAgentStatus(@PathVariable Long id,
                                                  @RequestParam String status) {
        Agent a = agentService.updateAgentStatus(id, status);
        return a != null ? ApiResponse.success(a) : ApiResponse.error(404, "坐席不存在");
    }

    // ==================== Session Queue ====================

    @GetMapping("/sessions")
    public ApiResponse<List<CustomerServiceSession>> listSessions(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(agentService.listSessions(status));
    }

    @PostMapping("/sessions")
    public ApiResponse<CustomerServiceSession> createSession(
            @RequestParam String userId,
            @RequestParam String userName,
            @RequestParam String userPhone) {
        return ApiResponse.success(agentService.createSession(userId, userName, userPhone));
    }

    // ==================== Agent Assignment ====================

    @PostMapping("/sessions/{sessionId}/assign")
    public ApiResponse<CustomerServiceSession> assignAgent(@PathVariable String sessionId,
                                                             @RequestParam Long agentId) {
        CustomerServiceSession s = agentService.assignAgent(sessionId, agentId);
        return s != null ? ApiResponse.success(s) : ApiResponse.error(400, "分配失败，坐席不可用");
    }

    @PostMapping("/sessions/{sessionId}/auto-assign")
    public ApiResponse<CustomerServiceSession> autoAssign(@PathVariable String sessionId) {
        CustomerServiceSession s = agentService.autoAssign(sessionId);
        return s != null ? ApiResponse.success(s) : ApiResponse.error(400, "自动分配失败");
    }

    // ==================== Chat Messages ====================

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<ChatMessage> sendMessage(@PathVariable String sessionId,
                                                  @RequestParam String role,
                                                  @RequestBody Map<String, String> body) {
        String content = body.get("content");
        ChatMessage msg = agentService.sendCsMessage(sessionId, role, content);
        return ApiResponse.success(msg);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessage>> getMessages(@PathVariable String sessionId) {
        return ApiResponse.success(agentService.getCsMessages(sessionId));
    }

    // ==================== Complete Session ====================

    @PostMapping("/sessions/{sessionId}/complete")
    public ApiResponse<CustomerServiceSession> completeSession(@PathVariable String sessionId) {
        CustomerServiceSession s = agentService.completeSession(sessionId);
        return s != null ? ApiResponse.success(s) : ApiResponse.error(404, "会话不存在");
    }
}
