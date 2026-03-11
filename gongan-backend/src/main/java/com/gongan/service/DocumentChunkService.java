package com.gongan.service;

import com.gongan.entity.AiDocumentChunk;

import java.util.List;

/**
 * 文档分块服务接口
 */
public interface DocumentChunkService {

    /**
     * 将文本内容分块
     * @param content 文本内容
     * @param chunkSize 分块大小（字符数）
     * @param overlap 重叠字符数
     * @return 分块列表
     */
    List<String> splitIntoChunks(String content, int chunkSize, int overlap);

    /**
     * 为文档创建分块并存储
     * @param docId 文档ID
     * @param kbId 知识库ID
     * @param content 文档内容
     * @return 创建的分块数量
     */
    int createChunksForDocument(Long docId, Long kbId, String content);

    /**
     * 删除文档的所有分块
     * @param docId 文档ID
     */
    void deleteChunksByDocId(Long docId);

    /**
     * 获取知识库的所有分块
     * @param kbId 知识库ID
     * @return 分块列表
     */
    List<AiDocumentChunk> getChunksByKbId(Long kbId);
}
