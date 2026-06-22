package com.hzmall.ai.model;

import lombok.Data;
import java.util.List;

@Data
public class MemberInfo {
    private String phone;
    private String name;
    private String memberLevel; // regular / silver / gold / platinum / vip
    private String memberId;
    private long points;
    private double totalSpent;
    private String managerName;
    private String managerPhone;
    private List<String> tags;
    private boolean vip;

    public MemberInfo() {}

    public MemberInfo(String phone, String name, String memberLevel, String memberId,
                      long points, double totalSpent, String managerName, String managerPhone, List<String> tags) {
        this.phone = phone;
        this.name = name;
        this.memberLevel = memberLevel;
        this.memberId = memberId;
        this.points = points;
        this.totalSpent = totalSpent;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
        this.tags = tags;
        this.vip = "vip".equals(memberLevel) || "platinum".equals(memberLevel);
    }
}
