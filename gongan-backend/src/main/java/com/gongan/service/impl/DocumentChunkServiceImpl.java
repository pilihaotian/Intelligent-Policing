package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.gongan.entity.AiDocumentChunk;
import com.gongan.mapper.AiDocumentChunkMapper;
import com.gongan.service.DocumentChunkService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档分块服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl implements DocumentChunkService {

    private final AiDocumentChunkMapper chunkMapper;
    private final LLMClient llmClient;

    /**
     * 默认分块大小
     */
    private static final int DEFAULT_CHUNK_SIZE = 500;

    /**
     * 默认重叠大小
     */
    private static final int DEFAULT_OVERLAP = 50;

    @Override
    public List<String> splitIntoChunks(String content, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chunks;
        }

        // 按段落分割，保持语义完整性
        String[] paragraphs = content.split("\n+");
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            // 如果当前段落本身就超过分块大小，需要进一步分割
            if (paragraph.length() > chunkSize) {
                // 先保存当前累积的内容
                if (currentLength > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentLength = 0;
                }
                // 按句子分割长段落
                chunks.addAll(splitLongParagraph(paragraph, chunkSize, overlap));
            } else {
                // 检查是否需要创建新分块
                if (currentLength + paragraph.length() + 1 > chunkSize && currentLength > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // 保留部分重叠内容
                    String overlapText = getOverlapText(currentChunk.toString(), overlap);
                    currentChunk = new StringBuilder(overlapText);
                    currentLength = overlapText.length();
                }
                currentChunk.append(paragraph).append("\n");
                currentLength += paragraph.length() + 1;
            }
        }

        // 添加最后一个分块
        if (currentLength > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 分割长段落
     */
    private List<String> splitLongParagraph(String paragraph, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < paragraph.length()) {
            int end = Math.min(start + chunkSize, paragraph.length());
            
            // 尝试在句子边界处分割
            if (end < paragraph.length()) {
                int lastPeriod = paragraph.lastIndexOf("。", end);
                int lastQuestion = paragraph.lastIndexOf("？", end);
                int lastExclamation = paragraph.lastIndexOf("！", end);
                int lastSentenceEnd = Math.max(Math.max(lastPeriod, lastQuestion), lastExclamation);
                
                if (lastSentenceEnd > start + chunkSize / 2) {
                    end = lastSentenceEnd + 1;
                }
            }
            
            chunks.add(paragraph.substring(start, end).trim());
            start = end - overlap;
            if (start < 0) start = 0;
        }

        return chunks;
    }

    /**
     * 获取重叠文本
     */
    private String getOverlapText(String text, int overlapLength) {
        if (text.length() <= overlapLength) {
            return text;
        }
        return text.substring(text.length() - overlapLength);
    }

    @Override
    public int createChunksForDocument(Long docId, Long kbId, String content) {
        // 删除旧的分块
        chunkMapper.deleteByDocId(docId);

        // 分块
        List<String> chunks = splitIntoChunks(content, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
        
        int chunkIndex = 0;
        for (String chunkContent : chunks) {
            AiDocumentChunk chunk = new AiDocumentChunk();
            chunk.setDocId(docId);
            chunk.setKbId(kbId);
            chunk.setChunkIndex(chunkIndex++);
            chunk.setContent(chunkContent);
            chunk.setCharCount(chunkContent.length());
            
            // 生成向量
            try {
                List<Float> embedding = llmClient.getEmbedding(chunkContent);
                chunk.setEmbedding(JSON.toJSONString(embedding));
            } catch (Exception e) {
                log.warn("生成向量失败，分块索引: {}", chunkIndex - 1, e);
            }
            
            chunkMapper.insert(chunk);
        }

        log.info("文档分块完成，文档ID: {}, 分块数量: {}", docId, chunks.size());
        return chunks.size();
    }

    @Override
    public void deleteChunksByDocId(Long docId) {
        chunkMapper.deleteByDocId(docId);
    }

    @Override
    public List<AiDocumentChunk> getChunksByKbId(Long kbId) {
        return chunkMapper.selectByKbId(kbId);
    }
}
