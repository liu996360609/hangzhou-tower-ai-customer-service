package com.hzmall.ai.controller;

import com.hzmall.ai.model.ApiResponse;
import com.hzmall.ai.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ==================== Dashboard Metrics ====================

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard() {
        return ApiResponse.success(reportService.getDashboard());
    }

    // ==================== Session Statistics ====================

    @GetMapping("/session-stats")
    public ApiResponse<Map<String, Object>> getSessionStats() {
        return ApiResponse.success(reportService.getSessionStats());
    }

    // ==================== Hot Questions ====================

    @GetMapping("/hot-questions")
    public ApiResponse<Map<String, Object>> getHotQuestions() {
        return ApiResponse.success(reportService.getHotQuestions());
    }

    // ==================== Satisfaction Statistics ====================

    @GetMapping("/satisfaction")
    public ApiResponse<Map<String, Object>> getSatisfactionStats() {
        return ApiResponse.success(reportService.getSatisfactionStats());
    }
}
