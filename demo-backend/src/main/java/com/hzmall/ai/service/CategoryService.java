package com.hzmall.ai.service;

import com.hzmall.ai.model.Category;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final ConcurrentHashMap<Long, Category> categoryMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);

    @PostConstruct
    public void init() {
        categoryMap.clear();
        idGenerator.set(10);
        add(new Category(1L, "营业信息", "business_hours", "商场营业时间、节假日安排等信息", 1, 12, "active"));
        add(new Category(2L, "停车服务", "parking", "停车场位置、收费标准、优惠活动", 2, 8, "active"));
        add(new Category(3L, "品牌指南", "brands", "入驻品牌、楼层分布、品牌优惠", 3, 25, "active"));
        add(new Category(4L, "会员服务", "member", "会员权益、积分规则、等级制度", 4, 15, "active"));
        add(new Category(5L, "促销活动", "promotion", "当前促销活动、折扣信息", 5, 18, "active"));
    }

    public void add(Category c) { categoryMap.put(c.getId(), c); }

    public List<Category> list(String status) {
        return categoryMap.values().stream()
                .filter(c -> status == null || status.isEmpty() || c.getStatus().equals(status))
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .collect(Collectors.toList());
    }

    public Category getById(Long id) { return categoryMap.get(id); }

    public Category create(Category c) {
        c.setId(idGenerator.getAndIncrement());
        c.setCreatedAt(java.time.LocalDateTime.now());
        c.setUpdatedAt(java.time.LocalDateTime.now());
        if (c.getStatus() == null) c.setStatus("active");
        categoryMap.put(c.getId(), c);
        return c;
    }

    public Category update(Long id, Category c) {
        Category existing = categoryMap.get(id);
        if (existing == null) return null;
        if (c.getName() != null) existing.setName(c.getName());
        if (c.getCode() != null) existing.setCode(c.getCode());
        if (c.getDescription() != null) existing.setDescription(c.getDescription());
        if (c.getSortOrder() > 0) existing.setSortOrder(c.getSortOrder());
        if (c.getStatus() != null) existing.setStatus(c.getStatus());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        return existing;
    }

    public boolean delete(Long id) { return categoryMap.remove(id) != null; }

    public void reset() { init(); }
}
