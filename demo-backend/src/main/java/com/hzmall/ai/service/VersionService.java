package com.hzmall.ai.service;

import com.hzmall.ai.model.KnowledgeVersion;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VersionService {
    private final ConcurrentHashMap<Long, KnowledgeVersion> versionMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);

    @PostConstruct
    public void init() {
        versionMap.clear();
        idGenerator.set(10);
        KnowledgeVersion v1 = new KnowledgeVersion(1L, "V1.0 - 初始版本", "上线初始知识库", 50, "张三");
        v1.setStatus("published");
        v1.setPublishedAt(java.time.LocalDateTime.of(2024, 1, 15, 10, 0));
        versionMap.put(1L, v1);

        KnowledgeVersion v2 = new KnowledgeVersion(2L, "V1.1 - 春节更新", "增加春节营业时间和促销信息", 62, "李四");
        v2.setStatus("published");
        v2.setPublishedAt(java.time.LocalDateTime.of(2024, 2, 1, 9, 0));
        versionMap.put(2L, v2);

        KnowledgeVersion v3 = new KnowledgeVersion(3L, "V1.2 - 春季促销", "更新春季活动和新品牌信息", 75, "张三");
        v3.setStatus("published");
        v3.setPublishedAt(java.time.LocalDateTime.of(2024, 3, 1, 10, 0));
        versionMap.put(3L, v3);

        KnowledgeVersion v4 = new KnowledgeVersion(4L, "V1.3 - 4月更新", "新增会员权益和停车优惠", 85, "李四");
        v4.setStatus("draft");
        versionMap.put(4L, v4);
    }

    public List<KnowledgeVersion> list() { return new ArrayList<>(versionMap.values()); }

    public KnowledgeVersion getById(Long id) { return versionMap.get(id); }

    public KnowledgeVersion create(KnowledgeVersion v) {
        v.setId(idGenerator.getAndIncrement());
        v.setCreatedAt(java.time.LocalDateTime.now());
        versionMap.put(v.getId(), v);
        return v;
    }

    public KnowledgeVersion publish(Long id) {
        KnowledgeVersion v = versionMap.get(id);
        if (v != null) {
            v.setStatus("published");
            v.setPublishedAt(java.time.LocalDateTime.now());
        }
        return v;
    }

    public KnowledgeVersion archive(Long id) {
        KnowledgeVersion v = versionMap.get(id);
        if (v != null) v.setStatus("archived");
        return v;
    }

    public KnowledgeVersion rollback(Long id) {
        // 回滚到指定版本
        KnowledgeVersion v = versionMap.get(id);
        return v;
    }

    public void reset() { init(); }
}
