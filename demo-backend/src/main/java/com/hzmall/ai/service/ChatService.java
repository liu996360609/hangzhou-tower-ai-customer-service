package com.hzmall.ai.service;

import com.hzmall.ai.model.ChatMessage;
import com.hzmall.ai.model.ChatSession;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ConcurrentHashMap<String, ChatSession> sessionMap = new ConcurrentHashMap<>();
    private final AtomicLong messageSeq = new AtomicLong(100);

    // 模拟百炼AI回复模板
    private final Map<String, String> aiResponses = new HashMap<>();

    @PostConstruct
    public void init() {
        sessionMap.clear();
        messageSeq.set(100);
        aiResponses.put("营业", "杭州大厦正常营业时间为周一至周日 10:00-22:00。春节期间可能调整，请关注官方公告。");
        aiResponses.put("停车", "杭州大厦停车场收费标准：首小时免费，之后每小时10元，每日封顶80元。会员可享受停车优惠。");
        aiResponses.put("会员", "您可以通过微信小程序或线下服务台注册成为杭州大厦会员。会员享有积分、专属折扣、免费停车等权益。");
        aiResponses.put("优惠", "当前正在进行春季折扣季活动，全场8折起！美妆满1000减200，餐饮双人套餐特惠。");
        aiResponses.put("品牌", "杭州大厦汇聚了众多国际知名品牌，涵盖服饰、美妆、珠宝、餐饮等品类。您可以在微信小程序查看品牌楼层分布。");
        aiResponses.put("积分", "消费1元=1积分，100积分可抵扣1元现金。积分也可在积分商城兑换精美礼品，有效期为1年。");
    }

    public ChatSession createSession(String userId, String userName) {
        String sessionId = "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        ChatSession session = new ChatSession(sessionId, userId, userName);
        sessionMap.put(sessionId, session);
        return session;
    }

    public ChatSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public List<ChatMessage> getMessages(String sessionId) {
        ChatSession s = sessionMap.get(sessionId);
        return s != null ? s.getMessages() : new ArrayList<>();
    }

    // 模拟百炼API调用: 调用Bailian Agent进行对话
    // 实际调用: bailianClient.runAgent(sessionId, userMessage, knowledgeBaseId)
    public ChatMessage sendMessage(String sessionId, String userId, String content) {
        ChatSession session = sessionMap.get(sessionId);
        if (session == null) return null;

        // 添加用户消息
        ChatMessage userMsg = new ChatMessage(
                "msg_user_" + messageSeq.getAndIncrement(),
                sessionId, "user", content);
        if (session.getMessages() == null) session.setMessages(new ArrayList<>());
        session.getMessages().add(userMsg);
        session.setUpdatedAt(java.time.LocalDateTime.now());

        // 生成模拟AI回复
        String reply = generateMockReply(content);

        // 添加AI回复
        ChatMessage aiMsg = new ChatMessage(
                "msg_ai_" + messageSeq.getAndIncrement(),
                sessionId, "assistant", reply);
        session.getMessages().add(aiMsg);
        session.setUpdatedAt(java.time.LocalDateTime.now());

        return aiMsg;
    }

    public ChatSession transferToHuman(String sessionId, String reason) {
        ChatSession session = sessionMap.get(sessionId);
        if (session != null) {
            session.setTransferredToHuman(true);
            session.setTransferReason(reason);
            session.setStatus("transferred");

            ChatMessage notice = new ChatMessage(
                    "msg_notice_" + messageSeq.getAndIncrement(),
                    sessionId, "system",
                    "已将您的会话转接给人工客服，请稍等...");
            session.getMessages().add(notice);
        }
        return session;
    }

    public List<ChatSession> listSessions(String status) {
        return sessionMap.values().stream()
                .filter(s -> status == null || status.isEmpty() || s.getStatus().equals(status))
                .sorted(Comparator.comparing(ChatSession::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    private String generateMockReply(String content) {
        String lower = content.toLowerCase();
        // 匹配关键词
        for (Map.Entry<String, String> entry : aiResponses.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue() + "\n\n如果还有其他问题，请随时提问。如需人工帮助，请输入"转人工"。";
            }
        }
        // 默认回复
        String[] defaults = {
                "您好！感谢您的提问。根据知识库信息，我可以为您提供以下帮助：\n\n" +
                        "• 营业时间：10:00-22:00\n" +
                        "• 停车：首小时免费\n" +
                        "• 会员：消费1元=1积分\n\n" +
                        "如需更详细信息，请告诉我您关心的具体内容。",
                "您好！关于您的问题，我查询到以下信息：\n\n" +
                        "杭州大厦提供全方位的服务，包括购物、餐饮、娱乐等。\n" +
                        "您可以查看我们的品牌指南、会员权益或当前促销活动。\n\n" +
                        "如需人工客服帮助，请输入"转人工"。",
                "感谢您的提问！我理解您的需求。杭州大厦为您提供以下服务：\n\n" +
                        "1. 购物咨询 - 品牌信息、楼层分布\n" +
                        "2. 会员咨询 - 权益、积分\n" +
                        "3. 停车咨询 - 收费、优惠\n\n" +
                        "请告诉我您需要了解哪方面信息？"
        };
        return defaults[(int)(Math.random() * defaults.length)];
    }

    public void reset() { init(); }
}
