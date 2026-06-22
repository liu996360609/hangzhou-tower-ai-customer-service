package com.hzmall.ai.service;

import com.hzmall.ai.model.OperationLog;
import com.hzmall.ai.model.Role;
import com.hzmall.ai.model.SensitiveWord;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class SystemService {
    private final ConcurrentHashMap<Long, OperationLog> logMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Role> roleMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, SensitiveWord> sensitiveMap = new ConcurrentHashMap<>();
    private final AtomicLong logSeq = new AtomicLong(20);
    private final AtomicLong roleSeq = new AtomicLong(10);
    private final AtomicLong sensitiveSeq = new AtomicLong(10);

    @PostConstruct
    public void init() {
        logMap.clear();
        roleMap.clear();
        sensitiveMap.clear();
        logSeq.set(20);
        roleSeq.set(10);
        sensitiveSeq.set(10);

        // 角色
        addRole(new Role(1L, "超级管理员", "admin", "拥有所有权限",
                Arrays.asList("kb:*", "chat:*", "cs:*", "member:*", "report:*", "system:*")));
        roleMap.get(1L).setUserCount(2);

        addRole(new Role(2L, "知识库管理员", "kb_admin", "管理知识库",
                Arrays.asList("kb:read", "kb:write", "kb:review", "kb:publish")));
        roleMap.get(2L).setUserCount(3);

        addRole(new Role(3L, "客服坐席", "agent", "处理客户咨询",
                Arrays.asList("chat:read", "chat:write", "cs:read", "cs:write", "member:read")));
        roleMap.get(3L).setUserCount(5);

        addRole(new Role(4L, "数据分析师", "analyst", "查看数据报表",
                Arrays.asList("report:read", "kb:read")));
        roleMap.get(4L).setUserCount(2);

        addRole(new Role(5L, "审核员", "reviewer", "审核知识内容",
                Arrays.asList("kb:read", "kb:review")));
        roleMap.get(5L).setUserCount(2);

        // 敏感词
        addSensitiveWord(new SensitiveWord(1L, "垃圾", "辱骂", "block", "***"));
        addSensitiveWord(new SensitiveWord(2L, "投诉", "敏感", "replace", "[反馈]"));
        addSensitiveWord(new SensitiveWord(3L, "骗子", "辱骂", "block", "***"));
        addSensitiveWord(new SensitiveWord(4L, "退款", "业务", "replace", "[售后]"));

        // 操作日志
        addLog(new OperationLog(1L, "张三", "知识库", "上传文件", "上传文件: 2024春季促销活动方案.pptx"));
        addLog(new OperationLog(2L, "李四", "知识库", "审核通过", "审核通过文件: 停车场收费标准.docx"));
        addLog(new OperationLog(3L, "张三", "系统", "发布版本", "发布知识库版本: V1.3 - 4月更新"));
        addLog(new OperationLog(4L, "王五", "客服", "转接会话", "将用户会话转接给经理"));
        addLog(new OperationLog(5L, "赵六", "会员", "查询会员", "查询会员: 138****1001"));
    }

    public void addLog(OperationLog l) { logMap.put(l.getId(), l); }
    public void addRole(Role r) { roleMap.put(r.getId(), r); }
    public void addSensitiveWord(SensitiveWord s) { sensitiveMap.put(s.getId(), s); }

    public List<OperationLog> listLogs(String module) {
        if (module == null || module.isEmpty()) return new ArrayList<>(logMap.values());
        return logMap.values().stream()
                .filter(l -> l.getModule().equals(module))
                .collect(Collectors.toList());
    }

    public List<Role> listRoles() { return new ArrayList<>(roleMap.values()); }

    public Role getRole(Long id) { return roleMap.get(id); }

    public Role createRole(Role r) {
        r.setId(roleSeq.getAndIncrement());
        roleMap.put(r.getId(), r);
        return r;
    }

    public Role updateRole(Long id, Role r) {
        Role existing = roleMap.get(id);
        if (existing == null) return null;
        if (r.getName() != null) existing.setName(r.getName());
        if (r.getDescription() != null) existing.setDescription(r.getDescription());
        if (r.getPermissions() != null) existing.setPermissions(r.getPermissions());
        return existing;
    }

    public boolean deleteRole(Long id) { return roleMap.remove(id) != null; }

    public List<SensitiveWord> listSensitiveWords() { return new ArrayList<>(sensitiveMap.values()); }

    public SensitiveWord createSensitiveWord(SensitiveWord s) {
        s.setId(sensitiveSeq.getAndIncrement());
        sensitiveMap.put(s.getId(), s);
        return s;
    }

    public SensitiveWord updateSensitiveWord(Long id, SensitiveWord s) {
        SensitiveWord existing = sensitiveMap.get(id);
        if (existing == null) return null;
        if (s.getWord() != null) existing.setWord(s.getWord());
        if (s.getCategory() != null) existing.setCategory(s.getCategory());
        if (s.getAction() != null) existing.setAction(s.getAction());
        if (s.getReplacement() != null) existing.setReplacement(s.getReplacement());
        existing.setEnabled(s.isEnabled());
        return existing;
    }

    public boolean deleteSensitiveWord(Long id) { return sensitiveMap.remove(id) != null; }

    public void reset() { init(); }
}
