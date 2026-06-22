package com.hzmall.ai.service;

import com.hzmall.ai.model.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class CustomerServiceAgentService {
    private final ConcurrentHashMap<Long, Agent> agentMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CustomerServiceSession> csSessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ChatMessage>> csMessagesMap = new ConcurrentHashMap<>();
    private final AtomicLong agentSeq = new AtomicLong(10);
    private final AtomicLong csSeq = new AtomicLong(100);
    private final AtomicLong msgSeq = new AtomicLong(500);

    @PostConstruct
    public void init() {
        agentMap.clear();
        csSessionMap.clear();
        csMessagesMap.clear();
        agentSeq.set(10);
        csSeq.set(100);

        addAgent(new Agent(1L, "张三", "manager", "online", 0, 5));
        agentMap.get(1L).setTotalResolved(156);
        agentMap.get(1L).setSatisfactionRate(4.8);

        addAgent(new Agent(2L, "李四", "agent", "online", 2, 5));
        agentMap.get(2L).setTotalResolved(89);
        agentMap.get(2L).setSatisfactionRate(4.5);

        addAgent(new Agent(3L, "王五", "agent", "busy", 5, 5));
        agentMap.get(3L).setTotalResolved(123);
        agentMap.get(3L).setSatisfactionRate(4.6);

        addAgent(new Agent(4L, "赵六", "agent", "offline", 0, 5));
        agentMap.get(4L).setTotalResolved(67);
        agentMap.get(4L).setSatisfactionRate(4.3);
    }

    public void addAgent(Agent a) { agentMap.put(a.getId(), a); }

    public List<Agent> listAgents() { return new ArrayList<>(agentMap.values()); }

    public Agent getAgent(Long id) { return agentMap.get(id); }

    public Agent updateAgentStatus(Long id, String status) {
        Agent a = agentMap.get(id);
        if (a != null) a.setStatus(status);
        return a;
    }

    public List<CustomerServiceSession> listSessions(String status) {
        if (status == null || status.isEmpty()) return new ArrayList<>(csSessionMap.values());
        return csSessionMap.values().stream()
                .filter(s -> s.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    // 用户发起在线客服会话
    public CustomerServiceSession createSession(String userId, String userName, String userPhone) {
        String sessionId = "cs_" + System.currentTimeMillis();
        CustomerServiceSession session = new CustomerServiceSession(sessionId, userId, userName, userPhone);
        csSessionMap.put(sessionId, session);
        csMessagesMap.put(sessionId, new ArrayList<>());
        return session;
    }

    // VIP路由逻辑：VIP用户直接分配给会员经理，普通用户分配给空闲坐席
    public CustomerServiceSession assignAgent(String sessionId, Long agentId) {
        CustomerServiceSession session = csSessionMap.get(sessionId);
        Agent agent = agentMap.get(agentId);
        if (session == null || agent == null) return null;

        if (agent.getCurrentSessions() >= agent.getMaxSessions() || "offline".equals(agent.getStatus())) {
            return null;
        }

        session.setAgentId(agentId);
        session.setAgentName(agent.getName());
        session.setStatus("active");
        session.setAssignedAt(java.time.LocalDateTime.now());

        agent.setCurrentSessions(agent.getCurrentSessions() + 1);
        if (agent.getCurrentSessions() >= agent.getMaxSessions()) {
            agent.setStatus("busy");
        }

        return session;
    }

    public CustomerServiceSession autoAssign(String sessionId) {
        CustomerServiceSession session = csSessionMap.get(sessionId);
        if (session == null) return null;

        // VIP用户优先分配给经理
        boolean isVip = session.getUserPhone() != null && session.getUserPhone().startsWith("138");
        if (isVip) {
            for (Agent a : agentMap.values()) {
                if ("manager".equals(a.getRole()) && "online".equals(a.getStatus()) &&
                        a.getCurrentSessions() < a.getMaxSessions()) {
                    return assignAgent(sessionId, a.getId());
                }
            }
        }

        // 普通用户分配给空闲坐席
        for (Agent a : agentMap.values()) {
            if ("agent".equals(a.getRole()) && "online".equals(a.getStatus()) &&
                    a.getCurrentSessions() < a.getMaxSessions()) {
                return assignAgent(sessionId, a.getId());
            }
        }

        return session; // 无可用坐席，保持等待状态
    }

    public ChatMessage sendCsMessage(String sessionId, String role, String content) {
        List<ChatMessage> messages = csMessagesMap.computeIfAbsent(sessionId, k -> new ArrayList<>());
        ChatMessage msg = new ChatMessage(
                "cs_msg_" + msgSeq.getAndIncrement(),
                sessionId, role, content);
        messages.add(msg);
        return msg;
    }

    public List<ChatMessage> getCsMessages(String sessionId) {
        return csMessagesMap.getOrDefault(sessionId, new ArrayList<>());
    }

    public CustomerServiceSession completeSession(String sessionId) {
        CustomerServiceSession session = csSessionMap.get(sessionId);
        if (session != null) {
            session.setStatus("completed");
            session.setCompletedAt(java.time.LocalDateTime.now());
            // 释放坐席
            if (session.getAgentId() != null) {
                Agent agent = agentMap.get(session.getAgentId());
                if (agent != null) {
                    agent.setCurrentSessions(Math.max(0, agent.getCurrentSessions() - 1));
                    agent.setTotalResolved(agent.getTotalResolved() + 1);
                    if ("busy".equals(agent.getStatus())) {
                        agent.setStatus("online");
                    }
                }
            }
        }
        return session;
    }

    public void reset() { init(); }
}
