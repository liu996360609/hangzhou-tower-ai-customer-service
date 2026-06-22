package com.hzmall.ai.service;

import com.hzmall.ai.model.MemberInfo;
import com.hzmall.ai.model.SatisfactionRating;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MemberService {
    private final Map<String, MemberInfo> memberMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        memberMap.clear();
        memberMap.put("13800001001", new MemberInfo(
                "13800001001", "王总", "vip", "VIP000001",
                128000, 580000.0, "张三", "13800009001",
                Arrays.asList("高频消费", "奢侈品偏好", "生日月3月")));
        memberMap.put("13800001002", new MemberInfo(
                "13800001002", "李小姐", "platinum", "PT000045",
                56000, 230000.0, "张三", "13800009001",
                Arrays.asList("美妆偏好", "周末常客")));
        memberMap.put("13900002001", new MemberInfo(
                "13900002001", "陈先生", "gold", "GD000123",
                12000, 68000.0, null, null,
                Arrays.asList("餐饮偏好")));
        memberMap.put("13600003001", new MemberInfo(
                "13600003001", "刘女士", "silver", "SV000456",
                3500, 15000.0, null, null,
                Arrays.asList("偶尔购物")));
        memberMap.put("15000004001", new MemberInfo(
                "15000004001", "赵先生", "regular", "RG001234",
                500, 3200.0, null, null,
                Arrays.asList("新会员")));
    }

    public MemberInfo lookupByPhone(String phone) {
        return memberMap.get(phone);
    }

    public boolean isVip(String phone) {
        MemberInfo m = memberMap.get(phone);
        return m != null && m.isVip();
    }

    public void reset() { init(); }
}
