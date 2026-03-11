package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.gongan.entity.AiDocumentChunk;
import com.gongan.service.DocumentChunkService;
import com.gongan.service.VectorStoreService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 向量存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl implements VectorStoreService {

    private final DocumentChunkService chunkService;
    private final LLMClient llmClient;

    @Override
    public List<SearchResult> similaritySearch(Long kbId, String query, int topK) {
        // 获取查询向量
        List<Float> queryVector = llmClient.getEmbedding(query);

        // 获取知识库所有分块
        List<AiDocumentChunk> chunks = chunkService.getChunksByKbId(kbId);
        if (chunks.isEmpty()) {
            return List.of();
        }

        // 计算相似度并排序
        List<SearchResult> results = new ArrayList<>();
        for (AiDocumentChunk chunk : chunks) {
            if (chunk.getEmbedding() == null || chunk.getEmbedding().isBlank()) {
                continue;
            }
            try {
                List<Float> chunkVector = JSON.parseObject(chunk.getEmbedding(), new TypeReference<List<Float>>() {});
                double score = cosineSimilarity(queryVector, chunkVector);
                results.add(new SearchResult(chunk, score));
            } catch (Exception e) {
                log.warn("解析向量失败，分块ID: {}", chunk.getId(), e);
            }
        }

        // 按相似度降序排序，取topK
        return results.stream()
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .limit(topK)
                .toList();
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double a = vectorA.get(i);
            double b = vectorB.get(i);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
