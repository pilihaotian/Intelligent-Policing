package com.gongan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongan.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {

    /**
     * 根据会话ID查询消息列表
     */
    @Select("SELECT * FROM ai_chat_message WHERE session_id = #{sessionId} ORDER BY created_time ASC")
    List<AiChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 删除会话的所有消息
     */
    @Select("DELETE FROM ai_chat_message WHERE session_id = #{sessionId}")
    void deleteBySessionId(@Param("sessionId") String sessionId);
}
