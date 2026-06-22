package com.hzmall.ai.service;

import com.hzmall.ai.model.ReviewRecord;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ConcurrentHashMap<Long, ReviewRecord> reviewMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);

    @PostConstruct
    public void init() {
        reviewMap.clear();
        idGenerator.set(10);
        add(new ReviewRecord(1L, 1L, "2024年营业时间通知.pdf", "published"));
        reviewMap.get(1L).setReviewABy("张三");
        reviewMap.get(1L).setReviewAAt("2024-03-01 10:00");
        reviewMap.get(1L).setReviewBBy("李四");
        reviewMap.get(1L).setReviewBAt("2024-03-01 14:00");
        reviewMap.get(1L).setReviewAComment("内容准确，格式规范");
        reviewMap.get(1L).setReviewBComment("通过");
        add(new ReviewRecord(2L, 2L, "节假日营业安排.docx", "published"));
        add(new ReviewRecord(3L, 3L, "停车场收费标准.docx", "pending_review_a"));
        add(new ReviewRecord(4L, 6L, "新入驻品牌介绍.pdf", "pending_review_a"));
        add(new ReviewRecord(5L, 9L, "2024春季促销活动方案.pptx", "rejected"));
        reviewMap.get(5L).setRejectReason("缺少关键折扣信息，请补充");
    }

    public void add(ReviewRecord r) { reviewMap.put(r.getId(), r); }

    public List<ReviewRecord> list(String status) {
        if (status == null || status.isEmpty()) return new ArrayList<>(reviewMap.values());
        return reviewMap.values().stream()
                .filter(r -> r.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public ReviewRecord reviewA(Long id, String reviewer, String comment) {
        ReviewRecord r = reviewMap.get(id);
        if (r == null) return null;
        r.setReviewABy(reviewer);
        r.setReviewAAt(java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19));
        r.setReviewAComment(comment);
        r.setStatus("pending_review_b");
        return r;
    }

    public ReviewRecord reviewB(Long id, String reviewer, String comment) {
        ReviewRecord r = reviewMap.get(id);
        if (r == null) return null;
        r.setReviewBBy(reviewer);
        r.setReviewBAt(java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19));
        r.setReviewBComment(comment);
        r.setStatus("published");
        return r;
    }

    public ReviewRecord reject(Long id, String reason) {
        ReviewRecord r = reviewMap.get(id);
        if (r == null) return null;
        r.setStatus("rejected");
        r.setRejectReason(reason);
        return r;
    }

    public void reset() { init(); }
}
