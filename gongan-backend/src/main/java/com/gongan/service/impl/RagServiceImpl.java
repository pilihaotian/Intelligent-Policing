package com.gongan.service.impl;

import com.gongan.entity.AiDocumentChunk;
import com.gongan.entity.AiKnowledgeDocument;
import com.gongan.mapper.AiKnowledgeDocumentMapper;
import com.gongan.service.DocumentChunkService;
import com.gongan.service.DocumentParserService;
import com.gongan.service.RagService;
import com.gongan.service.VectorStoreService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * RAG检索增强生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final DocumentParserService documentParserService;
    private final DocumentChunkService chunkService;
    private final VectorStoreService vectorStoreService;
    private final LLMClient llmClient;
    private final AiKnowledgeDocumentMapper documentMapper;

    /**
     * 检索topK数量
     */
    private static final int TOP_K = 5;

    /**
     * 相似度阈值
     */
    private static final double SIMILARITY_THRESHOLD = 0.5;

    @Override
    public Map<String, Object> processDocument(AiKnowledgeDocument document) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 更新状态为处理中
            document.setStatus("PROCESSING");
            documentMapper.updateById(document);

            // 解析文档
            Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();
            String content = documentParserService.parseDocument(filePath, document.getDocType());

            if (content == null || content.isBlank()) {
                throw new RuntimeException("文档内容为空");
            }

            // 分块并向量化
            int chunkCount = chunkService.createChunksForDocument(document.getId(), document.getKbId(), content);

            // 更新文档状态
            document.setChunkCount(chunkCount);
            document.setStatus("READY");
            documentMapper.updateById(document);

            result.put("success", true);
            result.put("chunkCount", chunkCount);
            result.put("charCount", content.length());

            log.info("文档处理完成，文档ID: {}, 分块数: {}", document.getId(), chunkCount);

        } catch (Exception e) {
            log.error("文档处理失败，文档ID: {}", document.getId(), e);
            document.setStatus("FAILED");
            documentMapper.updateById(document);

            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> query(Long kbId, String question) {
        return queryWithContext(kbId, question, null);
    }

    @Override
    public Map<String, Object> queryWithContext(Long kbId, String question, List<Map<String, String>> history) {
        Map<String, Object> result = new HashMap<>();

        // 检索相关文档
        List<VectorStoreService.SearchResult> searchResults = vectorStoreService.similaritySearch(kbId, question, TOP_K);

        // 过滤低相似度结果
        List<VectorStoreService.SearchResult> relevantChunks = searchResults.stream()
                .filter(r -> r.score() >= SIMILARITY_THRESHOLD)
                .toList();

        List<Map<String, Object>> sources = new ArrayList<>();
        StringBuilder contextBuilder = new StringBuilder();

        for (VectorStoreService.SearchResult searchResult : relevantChunks) {
            AiDocumentChunk chunk = searchResult.chunk();
            contextBuilder.append(chunk.getContent()).append("\n\n");

            Map<String, Object> source = new HashMap<>();
            source.put("docId", chunk.getDocId());
            source.put("chunkId", chunk.getId());
            source.put("content", truncateContent(chunk.getContent(), 200));
            source.put("score", Math.round(searchResult.score() * 100) + "%");
            sources.add(source);
        }

        // 构建提示词
        String systemPrompt = buildSystemPrompt(contextBuilder.toString());

        // 构建消息列表（包含历史上下文）
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加历史消息（最近N轮对话）
        if (history != null && !history.isEmpty()) {
            int maxHistory = 10; // 最多保留10条历史消息
            int start = Math.max(0, history.size() - maxHistory);
            for (int i = start; i < history.size(); i++) {
                messages.add(history.get(i));
            }
        }
        
        // 添加当前问题
        messages.add(Map.of("role", "user", "content", question));

        // 调用LLM
        String answer = llmClient.chat(messages, systemPrompt);

        result.put("answer", answer);
        result.put("sources", sources);
        result.put("hasContext", !relevantChunks.isEmpty());

        return result;
    }

    @Override
    public void queryStream(Long kbId, String question, java.util.function.Consumer<String> onChunk) {
        // 检索相关文档
        List<VectorStoreService.SearchResult> searchResults = vectorStoreService.similaritySearch(kbId, question, TOP_K);

        // 过滤低相似度结果
        List<VectorStoreService.SearchResult> relevantChunks = searchResults.stream()
                .filter(r -> r.score() >= SIMILARITY_THRESHOLD)
                .toList();

        StringBuilder contextBuilder = new StringBuilder();
        for (VectorStoreService.SearchResult searchResult : relevantChunks) {
            contextBuilder.append(searchResult.chunk().getContent()).append("\n\n");
        }

        String systemPrompt = buildSystemPrompt(contextBuilder.toString());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", question));

        llmClient.chatStream(messages, systemPrompt, onChunk);
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的公安业务智能助手。\n\n");

        if (context != null && !context.isBlank()) {
            prompt.append("以下是从知识库中检索到的相关内容，请基于这些内容回答用户问题：\n\n");
            prompt.append("---\n");
            prompt.append(context);
            prompt.append("---\n\n");
            prompt.append("请基于以上内容回答用户的问题。如果检索内容与问题不相关，请明确告知用户并尝试根据你的知识回答。\n");
        } else {
            prompt.append("知识库中未找到相关内容，请根据你的专业知识回答用户问题。\n");
        }

        prompt.append("\n回答要求：\n");
        prompt.append("1. 回答要准确、专业、有逻辑性\n");
        prompt.append("2. 如果引用了检索内容，请标注出处\n");
        prompt.append("3. 如果问题超出了公安业务领域，请礼貌地告知用户\n");

        return prompt.toString();
    }

    /**
     * 截断内容用于显示
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}
