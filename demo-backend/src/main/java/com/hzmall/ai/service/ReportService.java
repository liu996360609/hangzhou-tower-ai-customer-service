package com.hzmall.ai.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {
    private final Random random = new Random(42); // 固定种子保证数据稳定

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // 核心指标
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSessions", 12847);
        metrics.put("todaySessions", 356);
        metrics.put("aiResolutionRate", 78.5);
        metrics.put("humanTransferRate", 21.5);
        metrics.put("avgResponseTime", 1.2);
        metrics.put("satisfactionScore", 4.5);
        metrics.put("totalKnowledgeItems", 186);
        metrics.put("pendingReviews", 3);
        dashboard.put("metrics", metrics);

        // 会话趋势 (最近7天)
        List<Map<String, Object>> sessionTrend = new ArrayList<>();
        String[] dates = {"06-05", "06-06", "06-07", "06-08", "06-09", "06-10", "06-11"};
        int[] counts = {289, 312, 278, 345, 398, 367, 356};
        int[] aiCounts = {225, 245, 210, 268, 310, 289, 280};
        for (int i = 0; i < dates.length; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", dates[i]);
            day.put("total", counts[i]);
            day.put("aiResolved", aiCounts[i]);
            day.put("humanHandled", counts[i] - aiCounts[i]);
            sessionTrend.add(day);
        }
        dashboard.put("sessionTrend", sessionTrend);

        // 渠道分布
        Map<String, Object> channelDist = new HashMap<>();
        channelDist.put("web", 4520);
        channelDist.put("wechat", 5680);
        channelDist.put("app", 2147);
        channelDist.put("miniProgram", 500);
        dashboard.put("channelDistribution", channelDist);

        return dashboard;
    }

    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new HashMap<>();

        // 时段分布
        List<Map<String, Object>> hourDist = new ArrayList<>();
        for (int h = 9; h <= 22; h++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("hour", String.format("%02d:00", h));
            entry.put("count", 10 + random.nextInt(50));
            hourDist.add(entry);
        }
        stats.put("hourDistribution", hourDist);

        // 坐席工作量
        List<Map<String, Object>> agentWorkload = new ArrayList<>();
        agentWorkload.add(Map.of("name", "张三", "role", "经理", "handled", 45, "avgTime", 3.2, "satisfaction", 4.8));
        agentWorkload.add(Map.of("name", "李四", "role", "坐席", "handled", 67, "avgTime", 2.8, "satisfaction", 4.5));
        agentWorkload.add(Map.of("name", "王五", "role", "坐席", "handled", 52, "avgTime", 3.0, "satisfaction", 4.6));
        stats.put("agentWorkload", agentWorkload);

        return stats;
    }

    public Map<String, Object> getHotQuestions() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> questions = new ArrayList<>();
        questions.add(Map.of("question", "营业时间", "count", 456, "trend", "up"));
        questions.add(Map.of("question", "停车收费", "count", 389, "trend", "up"));
        questions.add(Map.of("question", "会员权益", "count", 312, "trend", "stable"));
        questions.add(Map.of("question", "LV在几楼", "count", 278, "trend", "up"));
        questions.add(Map.of("question", "积分使用", "count", 234, "trend", "down"));
        questions.add(Map.of("question", "促销活动", "count", 198, "trend", "up"));
        questions.add(Map.of("question", "开发票", "count", 156, "trend", "stable"));
        questions.add(Map.of("question", "Wi-Fi密码", "count", 123, "trend", "down"));
        result.put("questions", questions);

        // 问题分类占比
        Map<String, Integer> categoryDist = new HashMap<>();
        categoryDist.put("营业信息", 845);
        categoryDist.put("停车服务", 612);
        categoryDist.put("品牌指南", 534);
        categoryDist.put("会员服务", 489);
        categoryDist.put("促销活动", 367);
        result.put("categoryDistribution", categoryDist);

        return result;
    }

    public Map<String, Object> getSatisfactionStats() {
        Map<String, Object> stats = new HashMap<>();

        // 评分分布
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("5星", 4520);
        distribution.put("4星", 3210);
        distribution.put("3星", 1567);
        distribution.put("2星", 890);
        distribution.put("1星", 234);
        stats.put("distribution", distribution);

        // 平均得分趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月"};
        double[] scores = {4.2, 4.3, 4.4, 4.3, 4.5, 4.5};
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", months[i]);
            entry.put("score", scores[i]);
            trend.add(entry);
        }
        stats.put("trend", trend);

        // 关键词分析
        List<String> positiveKeywords = Arrays.asList("及时", "专业", "热情", "耐心", "满意", "快速");
        List<String> negativeKeywords = Arrays.asList("等待", "不准确", "慢", "重复", "转接");
        stats.put("positiveKeywords", positiveKeywords);
        stats.put("negativeKeywords", negativeKeywords);

        return stats;
    }
}
