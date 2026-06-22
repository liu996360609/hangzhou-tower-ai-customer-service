package com.hzmall.ai.service;

import com.hzmall.ai.model.KnowledgeFile;
import com.hzmall.ai.model.ParseSettings;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class KnowledgeFileService {
    private final ConcurrentHashMap<Long, KnowledgeFile> fileMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);
    private ParseSettings parseSettings = ParseSettings.defaultSettings();

    @PostConstruct
    public void init() {
        fileMap.clear();
        idGenerator.set(10);
        add(new KnowledgeFile(1L, 1L, "2024年营业时间通知.pdf", "pdf", 245680, "parsed", 100, 45));
        add(new KnowledgeFile(2L, 1L, "节假日营业安排.docx", "docx", 128900, "parsed", 100, 32));
        add(new KnowledgeFile(3L, 2L, "停车场收费标准.docx", "docx", 89500, "parsed", 100, 20));
        add(new KnowledgeFile(4L, 2L, "VIP停车优惠说明.pdf", "pdf", 156200, "parsed", 100, 28));
        add(new KnowledgeFile(5L, 3L, "品牌楼层分布指南.xlsx", "xlsx", 342100, "parsed", 100, 65));
        add(new KnowledgeFile(6L, 3L, "新入驻品牌介绍.pdf", "pdf", 512000, "parsing", 60, 0));
        add(new KnowledgeFile(7L, 4L, "VIP会员权益说明.docx", "docx", 198400, "parsed", 100, 38));
        add(new KnowledgeFile(8L, 4L, "积分兑换规则.pdf", "pdf", 87300, "parsed", 100, 18));
        add(new KnowledgeFile(9L, 5L, "2024春季促销活动方案.pptx", "pptx", 2340000, "uploading", 30, 0));
        add(new KnowledgeFile(10L, 5L, "周年庆活动详情.docx", "docx", 156700, "parsed", 100, 42));
    }

    public void add(KnowledgeFile f) { fileMap.put(f.getId(), f); }

    public List<KnowledgeFile> list(Long categoryId) {
        if (categoryId == null) return new ArrayList<>(fileMap.values());
        return fileMap.values().stream()
                .filter(f -> f.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }

    public KnowledgeFile getById(Long id) { return fileMap.get(id); }

    public KnowledgeFile upload(KnowledgeFile f) {
        f.setId(idGenerator.getAndIncrement());
        f.setUploadStatus("uploading");
        f.setParseProgress(0);
        fileMap.put(f.getId(), f);
        // 模拟百炼API: ApplyFileUploadLease -> 上传文件 -> AddFile
        // 实际调用: bailianClient.applyFileUploadLease() / bailianClient.addFile()
        simulateParse(f);
        return f;
    }

    public boolean delete(Long id) { return fileMap.remove(id) != null; }

    public ParseSettings getParseSettings() { return parseSettings; }

    public ParseSettings updateParseSettings(ParseSettings settings) {
        if (settings.getSegmentMode() != null) parseSettings.setSegmentMode(settings.getSegmentMode());
        if (settings.getSegmentLength() > 0) parseSettings.setSegmentLength(settings.getSegmentLength());
        if (settings.getSegmentOverlap() >= 0) parseSettings.setSegmentOverlap(settings.getSegmentOverlap());
        if (settings.getSeparator() != null) parseSettings.setSeparator(settings.getSeparator());
        if (settings.getRemoveExtraWhitespace()) parseSettings.setRemoveExtraWhitespace(true);
        return parseSettings;
    }

    private void simulateParse(KnowledgeFile f) {
        // 模拟异步解析过程
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                f.setUploadStatus("parsing");
                f.setParseProgress(50);
                Thread.sleep(1500);
                f.setUploadStatus("parsed");
                f.setParseProgress(100);
                f.setChunkCount(20 + (int)(Math.random() * 50));
                f.setParsedAt(java.time.LocalDateTime.now());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void reset() { init(); }
}
