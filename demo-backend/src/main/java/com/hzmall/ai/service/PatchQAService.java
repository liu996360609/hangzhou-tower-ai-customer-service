package com.hzmall.ai.service;

import com.hzmall.ai.model.PatchQA;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class PatchQAService {
    private final ConcurrentHashMap<Long, PatchQA> patchMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);

    @PostConstruct
    public void init() {
        patchMap.clear();
        idGenerator.set(10);
        add(new PatchQA(1L, "今天开门了吗？", "杭州大厦已经开门营业，营业时间为10:00-22:00。", "开门,营业", 100));
        add(new PatchQA(2L, "有Wi-Fi吗？", "杭州大厦全场提供免费Wi-Fi，连接名称为HZMALL-Free，无需密码。", "wifi,网络", 90));
        add(new PatchQA(3L, "服务台在哪？", "服务台位于A座1层正门入口处和B座3层电梯旁。", "服务台,咨询", 90));
        add(new PatchQA(4L, "可以开发票吗？", "可以，请在结账时告知收银员需要发票，可提供电子发票或纸质发票。", "发票,开票", 80));
        add(new PatchQA(5L, "支持哪些支付方式？", "支持支付宝、微信支付、银联卡、信用卡、现金等支付方式。", "支付,付款", 80));
        patchMap.get(1L).setHitCount(234);
        patchMap.get(2L).setHitCount(156);
        patchMap.get(3L).setHitCount(89);
        patchMap.get(4L).setHitCount(67);
        patchMap.get(5L).setHitCount(45);
    }

    public void add(PatchQA p) { patchMap.put(p.getId(), p); }

    public List<PatchQA> list(Boolean enabled) {
        if (enabled == null) return new ArrayList<>(patchMap.values());
        return patchMap.values().stream()
                .filter(p -> p.isEnabled() == enabled)
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());
    }

    public PatchQA getById(Long id) { return patchMap.get(id); }

    public PatchQA create(PatchQA p) {
        p.setId(idGenerator.getAndIncrement());
        p.setCreatedAt(java.time.LocalDateTime.now());
        p.setUpdatedAt(java.time.LocalDateTime.now());
        patchMap.put(p.getId(), p);
        return p;
    }

    public PatchQA update(Long id, PatchQA p) {
        PatchQA existing = patchMap.get(id);
        if (existing == null) return null;
        if (p.getQuestion() != null) existing.setQuestion(p.getQuestion());
        if (p.getAnswer() != null) existing.setAnswer(p.getAnswer());
        if (p.getTriggerKeywords() != null) existing.setTriggerKeywords(p.getTriggerKeywords());
        if (p.getPriority() > 0) existing.setPriority(p.getPriority());
        existing.setEnabled(p.isEnabled());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        return existing;
    }

    public boolean delete(Long id) { return patchMap.remove(id) != null; }

    public PatchQA hit(Long id) {
        PatchQA p = patchMap.get(id);
        if (p != null) p.setHitCount(p.getHitCount() + 1);
        return p;
    }

    public void reset() { init(); }
}
