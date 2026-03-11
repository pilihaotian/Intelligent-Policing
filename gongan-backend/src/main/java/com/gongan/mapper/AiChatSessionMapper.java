package com.gongan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongan.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {

    /**
     * 根据用户ID查询会话列表
     */
    @Select("SELECT * FROM ai_chat_session WHERE user_id = #{userId} ORDER BY updated_time DESC")
    List<AiChatSession> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID查询
     */
    @Select("SELECT * FROM ai_chat_session WHERE session_id = #{sessionId}")
    AiChatSession selectBySessionId(@Param("sessionId") String sessionId);
}
