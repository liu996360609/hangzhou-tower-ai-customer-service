package com.hzmall.ai.service;

import com.hzmall.ai.model.KnowledgeItem;
import com.hzmall.ai.model.RetrieveResult;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {
    private final ConcurrentHashMap<Long, KnowledgeItem> knowledgeMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(20);

    @PostConstruct
    public void init() {
        knowledgeMap.clear();
        idGenerator.set(20);
        add(new KnowledgeItem(1L, "杭州大厦的营业时间是什么？",
                "杭州大厦正常营业时间为周一至周日 10:00-22:00。节假日可能调整，请关注官方公告。",
                "营业信息", Arrays.asList("营业时间", "基础信息"), "2024年营业时间通知.pdf"));
        add(new KnowledgeItem(2L, "春节营业时间安排？",
                "春节期间（除夕至初六）营业时间调整为 11:00-20:00，初七恢复正常。",
                "营业信息", Arrays.asList("春节", "节假日"), "节假日营业安排.docx"));
        add(new KnowledgeItem(3L, "停车场怎么收费？",
                "停车场收费标准：首小时免费，之后每小时10元，每日封顶80元。会员可享受停车优惠。",
                "停车服务", Arrays.asList("停车", "收费"), "停车场收费标准.docx"));
        add(new KnowledgeItem(4L, "VIP会员停车有优惠吗？",
                "VIP会员每日可免费停车4小时，超出部分享受5折优惠。铂金会员每日免费停车8小时。",
                "停车服务", Arrays.asList("VIP", "停车优惠"), "VIP停车优惠说明.pdf"));
        add(new KnowledgeItem(5L, "LV在几楼？",
                "Louis Vuitton专柜位于A座1层L108商铺。营业时间为10:00-22:00。",
                "品牌指南", Arrays.asList("LV", "楼层"), "品牌楼层分布指南.xlsx"));
        add(new KnowledgeItem(6L, "如何成为会员？",
                "您可以通过以下方式成为杭州大厦会员：1) 线下服务台办理 2) 微信小程序注册 3) 单笔消费满1000元自动注册。",
                "会员服务", Arrays.asList("会员", "注册"), "VIP会员权益说明.docx"));
        add(new KnowledgeItem(7L, "会员有什么权益？",
                "杭州大厦会员权益包括：1) 消费积分 2) 生日礼遇 3) 专属折扣 4) 免费停车 5) VIP休息室 6) 专属客服。",
                "会员服务", Arrays.asList("会员权益"), "VIP会员权益说明.docx"));
        add(new KnowledgeItem(8L, "积分怎么使用？",
                "积分可在消费时抵扣现金，100积分=1元。也可在积分商城兑换礼品。积分有效期为1年。",
                "会员服务", Arrays.asList("积分", "使用"), "积分兑换规则.pdf"));
        add(new KnowledgeItem(9L, "最近有什么促销活动？",
                "当前促销活动：1) 春季折扣季（3月1日-31日）全场8折起 2) 美妆满1000减200 3) 餐饮双人套餐特惠。",
                "促销活动", Arrays.asList("促销", "折扣"), "2024春季促销活动方案.pptx"));
        add(new KnowledgeItem(10L, "周年庆活动什么时候？",
                "杭州大厦周年庆活动将于4月15日-4月30日举行，届时全场低至5折，会员双倍积分。",
                "促销活动", Arrays.asList("周年庆"), "周年庆活动详情.docx"));
    }

    public void add(KnowledgeItem k) { knowledgeMap.put(k.getId(), k); }

    public List<KnowledgeItem> list(String category, String status, String keyword) {
        return knowledgeMap.values().stream()
                .filter(k -> category == null || category.isEmpty() || k.getCategory().equals(category))
                .filter(k -> status == null || status.isEmpty() || k.getStatus().equals(status))
                .filter(k -> keyword == null || keyword.isEmpty() ||
                        k.getQuestion().contains(keyword) || k.getAnswer().contains(keyword))
                .sorted(Comparator.comparing(KnowledgeItem::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public KnowledgeItem getById(Long id) { return knowledgeMap.get(id); }

    public KnowledgeItem create(KnowledgeItem k) {
        k.setId(idGenerator.getAndIncrement());
        k.setCreatedAt(java.time.LocalDateTime.now());
        k.setUpdatedAt(java.time.LocalDateTime.now());
        if (k.getStatus() == null) k.setStatus("draft");
        knowledgeMap.put(k.getId(), k);
        return k;
    }

    public KnowledgeItem update(Long id, KnowledgeItem k) {
        KnowledgeItem existing = knowledgeMap.get(id);
        if (existing == null) return null;
        if (k.getQuestion() != null) existing.setQuestion(k.getQuestion());
        if (k.getAnswer() != null) existing.setAnswer(k.getAnswer());
        if (k.getCategory() != null) existing.setCategory(k.getCategory());
        if (k.getTags() != null) existing.setTags(k.getTags());
        if (k.getStatus() != null) existing.setStatus(k.getStatus());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        return existing;
    }

    public boolean delete(Long id) { return knowledgeMap.remove(id) != null; }

    // 模拟百炼API检索: Retrieve
    // 实际调用: bailianClient.retrieve(query, topK, scoreThreshold)
    public List<RetrieveResult> retrieve(String query, int topK) {
        List<RetrieveResult> results = new ArrayList<>();
        for (KnowledgeItem k : knowledgeMap.values()) {
            if (!"published".equals(k.getStatus())) continue;
            double score = calculateRelevance(query, k);
            if (score > 0.1) {
                results.add(new RetrieveResult(k.getQuestion(), k.getAnswer(), score, k.getSource(), k.getCategory()));
            }
        }
        results.sort(Comparator.comparingDouble(RetrieveResult::getScore).reversed());
        if (results.size() > topK) {
            results = results.subList(0, topK);
        }
        return results;
    }

    private double calculateRelevance(String query, KnowledgeItem k) {
        String[] queryWords = query.toLowerCase().split("");
        String text = (k.getQuestion() + " " + k.getAnswer()).toLowerCase();
        int matchCount = 0;
        for (String word : queryWords) {
            if (text.contains(word)) matchCount++;
        }
        double base = (double) matchCount / Math.max(queryWords.length, 1);
        // 添加一些随机性模拟真实检索
        return Math.min(0.95, base + Math.random() * 0.15);
    }

    public void reset() { init(); }
}
