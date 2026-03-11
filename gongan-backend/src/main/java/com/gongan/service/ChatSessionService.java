package com.gongan.service;

import com.gongan.entity.AiChatMessage;
import com.gongan.entity.AiChatSession;
import com.gongan.filter.UserPrincipal;

import java.util.List;
import java.util.Map;

/**
 * 聊天会话服务接口
 */
public interface ChatSessionService {

    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<AiChatSession> listSessions(Long userId);

    /**
     * 创建新会话
     * @param userId 用户ID
     * @param kbId 知识库ID（可选）
     * @return 新会话
     */
    AiChatSession createSession(Long userId, Long kbId);

    /**
     * 获取会话详情
     * @param sessionId 会话ID
     * @return 会话信息
     */
    AiChatSession getSession(String sessionId);

    /**
     * 获取会话的消息历史
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<AiChatMessage> getMessages(String sessionId);

    /**
     * 删除会话
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 发送消息并获取回复
     * @param sessionId 会话ID
     * @param question 问题
     * @param user 当前用户
     * @return 回复结果
     */
    Map<String, Object> sendMessage(String sessionId, String question, UserPrincipal user);
}
