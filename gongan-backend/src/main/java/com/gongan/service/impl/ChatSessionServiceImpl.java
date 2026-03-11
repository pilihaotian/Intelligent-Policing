package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.gongan.entity.AiChatMessage;
import com.gongan.entity.AiChatSession;
import com.gongan.filter.UserPrincipal;
import com.gongan.mapper.AiChatMessageMapper;
import com.gongan.mapper.AiChatSessionMapper;
import com.gongan.service.ChatSessionService;
import com.gongan.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 聊天会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final RagService ragService;

    @Override
    public List<AiChatSession> listSessions(Long userId) {
        return sessionMapper.selectByUserId(userId);
    }

    @Override
    public AiChatSession createSession(Long userId, Long kbId) {
        AiChatSession session = new AiChatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setKbId(kbId);
        session.setTitle("新对话");
        session.setCreatedTime(LocalDateTime.now());
        session.setUpdatedTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public AiChatSession getSession(String sessionId) {
        return sessionMapper.selectBySessionId(sessionId);
    }

    @Override
    public List<AiChatMessage> getMessages(String sessionId) {
        return messageMapper.selectBySessionId(sessionId);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        // 删除消息
        messageMapper.deleteBySessionId(sessionId);
        // 删除会话
        AiChatSession session = sessionMapper.selectBySessionId(sessionId);
        if (session != null) {
            sessionMapper.deleteById(session.getId());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> sendMessage(String sessionId, String question, UserPrincipal user) {
        // 获取或创建会话
        AiChatSession session = sessionMapper.selectBySessionId(sessionId);
        if (session == null) {
            session = createSession(user.getUserId(), null);
            sessionId = session.getSessionId();
        }

        // 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setCreatedTime(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 获取历史消息用于上下文（不包含刚插入的用户消息）
        List<AiChatMessage> history = messageMapper.selectBySessionId(sessionId);
        
        // 转换为LLM需要的格式
        List<Map<String, String>> historyMessages = new ArrayList<>();
        for (AiChatMessage msg : history) {
            // 跳过最后一条（刚插入的用户消息）
            if (msg.getId().equals(userMessage.getId())) continue;
            Map<String, String> historyMsg = new HashMap<>();
            historyMsg.put("role", msg.getRole());
            historyMsg.put("content", msg.getContent());
            historyMessages.add(historyMsg);
        }

        // 调用RAG服务获取回答（带历史上下文）
        Map<String, Object> ragResult = ragService.queryWithContext(session.getKbId(), question, historyMessages);
        String answer = (String) ragResult.get("answer");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sources = (List<Map<String, Object>>) ragResult.get("sources");

        // 保存助手消息
        AiChatMessage assistantMessage = new AiChatMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        assistantMessage.setSources(sources != null ? JSON.toJSONString(sources) : null);
        assistantMessage.setCreatedTime(LocalDateTime.now());
        messageMapper.insert(assistantMessage);

        // 更新会话标题和更新时间
        if ("新对话".equals(session.getTitle())) {
            session.setTitle(truncateTitle(question, 20));
        }
        session.setUpdatedTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("answer", answer);
        result.put("sources", sources);
        return result;
    }

    /**
     * 截取标题
     */
    private String truncateTitle(String text, int maxLength) {
        if (text == null) return "新对话";
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
