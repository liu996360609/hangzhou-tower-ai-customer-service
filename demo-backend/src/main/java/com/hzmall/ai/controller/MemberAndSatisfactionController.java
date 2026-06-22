package com.hzmall.ai.controller;

import com.hzmall.ai.model.*;
import com.hzmall.ai.service.MemberService;
import com.hzmall.ai.service.SatisfactionService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class MemberAndSatisfactionController {

    private final MemberService memberService;
    private final SatisfactionService satisfactionService;

    public MemberAndSatisfactionController(MemberService memberService,
                                            SatisfactionService satisfactionService) {
        this.memberService = memberService;
        this.satisfactionService = satisfactionService;
    }

    // ==================== Member Service ====================

    @GetMapping("/member/lookup")
    public ApiResponse<MemberInfo> lookupMember(@RequestParam String phone) {
        MemberInfo member = memberService.lookupByPhone(phone);
        if (member != null) {
            return ApiResponse.success(member);
        }
        return ApiResponse.error(404, "会员不存在");
    }

    @GetMapping("/member/vip-check")
    public ApiResponse<Map<String, Object>> checkVip(@RequestParam String phone) {
        boolean isVip = memberService.isVip(phone);
        Map<String, Object> result = new HashMap<>();
        result.put("phone", phone);
        result.put("isVip", isVip);
        if (isVip) {
            result.put("routeTo", "vip_manager");
            result.put("message", "VIP用户，将路由至专属会员经理");
        } else {
            result.put("routeTo", "general_agent");
            result.put("message", "普通用户，将分配至在线客服坐席");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/member/manager-assign")
    public ApiResponse<Map<String, Object>> assignManager(@RequestParam String phone) {
        MemberInfo member = memberService.lookupByPhone(phone);
        Map<String, Object> result = new HashMap<>();
        if (member != null && member.isVip()) {
            result.put("assigned", true);
            result.put("managerName", member.getManagerName());
            result.put("managerPhone", member.getManagerPhone());
            result.put("memberLevel", member.getMemberLevel());
            return ApiResponse.success("已分配专属会员经理", result);
        }
        result.put("assigned", false);
        result.put("message", "非VIP用户，无需分配专属经理");
        return ApiResponse.success(result);
    }

    // ==================== Satisfaction Rating ====================

    @PostMapping("/satisfaction")
    public ApiResponse<SatisfactionRating> submitRating(
            @RequestParam String sessionId,
            @RequestParam int score,
            @RequestParam(required = false) String comment) {
        if (score < 1 || score > 5) {
            return ApiResponse.error(400, "评分必须在1-5之间");
        }
        SatisfactionRating rating = satisfactionService.submit(sessionId, score, comment);
        return ApiResponse.success(rating);
    }

    @GetMapping("/satisfaction")
    public ApiResponse<List<SatisfactionRating>> listRatings() {
        return ApiResponse.success(satisfactionService.list());
    }

    @GetMapping("/satisfaction/stats")
    public ApiResponse<Map<String, Object>> getSatisfactionStats() {
        return ApiResponse.success(satisfactionService.getStatistics());
    }
}
