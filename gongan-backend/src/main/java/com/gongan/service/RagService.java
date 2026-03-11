package com.gongan.service;

import com.gongan.entity.AiKnowledgeDocument;

import java.util.List;
import java.util.Map;

/**
 * RAG检索增强生成服务接口
 */
public interface RagService {

    /**
     * 处理文档（解析、分块、向量化）
     * @param document 文档实体
     * @return 处理结果
     */
    Map<String, Object> processDocument(AiKnowledgeDocument document);

    /**
     * RAG问答（无历史上下文）
     * @param kbId 知识库ID
     * @param question 问题
     * @return 回答结果
     */
    Map<String, Object> query(Long kbId, String question);

    /**
     * RAG问答（带历史上下文）
     * @param kbId 知识库ID
     * @param question 当前问题
     * @param history 历史消息列表，每条消息包含role和content
     * @return 回答结果
     */
    Map<String, Object> queryWithContext(Long kbId, String question, List<Map<String, String>> history);

    /**
     * RAG问答（流式）
     * @param kbId 知识库ID
     * @param question 问题
     * @param onChunk 流式回调
     */
    void queryStream(Long kbId, String question, java.util.function.Consumer<String> onChunk);
}
