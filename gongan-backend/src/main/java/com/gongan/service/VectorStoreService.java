package com.gongan.service;

import com.gongan.entity.AiDocumentChunk;

import java.util.List;

/**
 * 向量存储服务接口
 */
public interface VectorStoreService {

    /**
     * 相似度搜索
     * @param kbId 知识库ID
     * @param query 查询文本
     * @param topK 返回数量
     * @return 相似的文档分块
     */
    List<SearchResult> similaritySearch(Long kbId, String query, int topK);

    /**
     * 搜索结果
     */
    record SearchResult(AiDocumentChunk chunk, double score) {}
}
