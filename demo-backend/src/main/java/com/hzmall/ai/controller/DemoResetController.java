package com.hzmall.ai.controller;

import com.hzmall.ai.model.ApiResponse;
import com.hzmall.ai.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo Reset 端点 - 重置所有Mock数据到初始状态
 */
@RestController
@RequestMapping("/api/v1/demo")
public class DemoResetController {

    private final CategoryService categoryService;
    private final KnowledgeFileService knowledgeFileService;
    private final ReviewService reviewService;
    private final KnowledgeService knowledgeService;
    private final PatchQAService patchQAService;
    private final VersionService versionService;
    private final ChatService chatService;
    private final CustomerServiceAgentService agentService;
    private final MemberService memberService;
    private final SatisfactionService satisfactionService;
    private final SystemService systemService;

    public DemoResetController(CategoryService categoryService,
                                KnowledgeFileService knowledgeFileService,
                                ReviewService reviewService,
                                KnowledgeService knowledgeService,
                                PatchQAService patchQAService,
                                VersionService versionService,
                                ChatService chatService,
                                CustomerServiceAgentService agentService,
                                MemberService memberService,
                                SatisfactionService satisfactionService,
                                SystemService systemService) {
        this.categoryService = categoryService;
        this.knowledgeFileService = knowledgeFileService;
        this.reviewService = reviewService;
        this.knowledgeService = knowledgeService;
        this.patchQAService = patchQAService;
        this.versionService = versionService;
        this.chatService = chatService;
        this.agentService = agentService;
        this.memberService = memberService;
        this.satisfactionService = satisfactionService;
        this.systemService = systemService;
    }

    @PostMapping("/reset")
    public ApiResponse<Map<String, Object>> reset() {
        categoryService.reset();
        knowledgeFileService.reset();
        reviewService.reset();
        knowledgeService.reset();
        patchQAService.reset();
        versionService.reset();
        chatService.reset();
        agentService.reset();
        memberService.reset();
        satisfactionService.reset();
        systemService.reset();

        Map<String, Object> result = new HashMap<>();
        result.put("message", "所有Mock数据已重置到初始状态");
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        return ApiResponse.success(result);
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "hzmall-ai-customer-service");
        health.put("version", "1.0.0");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ApiResponse.success(health);
    }
}
