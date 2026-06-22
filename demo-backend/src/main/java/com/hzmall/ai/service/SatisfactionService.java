package com.hzmall.ai.service;

import com.hzmall.ai.model.SatisfactionRating;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class SatisfactionService {
    private final ConcurrentHashMap<Long, SatisfactionRating> ratingMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(20);

    @PostConstruct
    public void init() {
        ratingMap.clear();
        idGenerator.set(20);
        add(new SatisfactionRating(1L, "session_001", 5, "回复非常及时，服务态度好！"));
        add(new SatisfactionRating(2L, "session_002", 4, "问题解决了，很好"));
        add(new SatisfactionRating(3L, "session_003", 5, "VIP服务很到位"));
        add(new SatisfactionRating(4L, "session_004", 3, "等待时间有点长"));
        add(new SatisfactionRating(5L, "session_005", 4, "客服很专业"));
        add(new SatisfactionRating(6L, "session_006", 5, "非常满意的体验"));
        add(new SatisfactionRating(7L, "session_007", 2, "回答不够准确"));
        add(new SatisfactionRating(8L, "session_008", 4, "整体还不错"));
    }

    public void add(SatisfactionRating r) { ratingMap.put(r.getId(), r); }

    public SatisfactionRating submit(String sessionId, int score, String comment) {
        SatisfactionRating rating = new SatisfactionRating(
                idGenerator.getAndIncrement(), sessionId, score, comment);
        ratingMap.put(rating.getId(), rating);
        return rating;
    }

    public List<SatisfactionRating> list() { return new ArrayList<>(ratingMap.values()); }

    public Map<String, Object> getStatistics() {
        List<SatisfactionRating> ratings = new ArrayList<>(ratingMap.values());
        double avg = ratings.stream().mapToInt(SatisfactionRating::getScore).average().orElse(0);
        Map<String, Long> distribution = ratings.stream()
                .collect(Collectors.groupingBy(SatisfactionRating::getScore, Collectors.counting()));

        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalRatings", ratings.size());
        stats.put("averageScore", Math.round(avg * 100.0) / 100.0);
        stats.put("distribution", distribution);
        stats.put("satisfactionRate", ratings.stream().filter(r -> r.getScore() >= 4).count() * 100.0 / ratings.size());
        return stats;
    }

    public void reset() { init(); }
}
