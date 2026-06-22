package com.hzmall.ai.controller;

import com.hzmall.ai.model.*;
import com.hzmall.ai.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ==================== Session Management ====================

    @PostMapping("/sessions")
    public ApiResponse<ChatSession> createSession(
            @RequestParam String userId,
            @RequestParam String userName) {
        return ApiResponse.success(chatService.createSession(userId, userName));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> listSessions(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(chatService.listSessions(status));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession s = chatService.getSession(sessionId);
        return s != null ? ApiResponse.success(s) : ApiResponse.error(404, "会话不存在");
    }

    // ==================== Multi-turn Chat ====================

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessage>> getMessages(@PathVariable String sessionId) {
        return ApiResponse.success(chatService.getMessages(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<ChatMessage> sendMessage(
            @PathVariable String sessionId,
            @RequestParam String userId,
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isEmpty()) {
            return ApiResponse.error(400, "消息内容不能为空");
        }
        ChatMessage reply = chatService.sendMessage(sessionId, userId, content);
        return reply != null ? ApiResponse.success(reply) : ApiResponse.error(404, "会话不存在");
    }

    // ==================== Transfer to Human ====================

    @PostMapping("/sessions/{sessionId}/transfer")
    public ApiResponse<ChatSession> transferToHuman(
            @PathVariable String sessionId,
            @RequestParam(required = false, defaultValue = "用户请求人工服务") String reason) {
        ChatSession s = chatService.transferToHuman(sessionId, reason);
        return s != null ? ApiResponse.success(s) : ApiResponse.error(404, "会话不存在");
    }

    // ==================== Chat History ====================

    @GetMapping("/history")
    public ApiResponse<List<ChatSession>> getHistory(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status) {
        List<ChatSession> sessions = chatService.listSessions(status);
        if (userId != null) {
            sessions.removeIf(s -> !s.getUserId().equals(userId));
        }
        return ApiResponse.success(sessions);
    }
}
