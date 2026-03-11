package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档分块实体
 * 注意：使用JSON存储向量，避免依赖pgvector扩展
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_document_chunk")
public class AiDocumentChunk extends BaseEntity {

    /**
     * 文档ID
     */
    private Long docId;

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 向量数据（JSON格式存储）
     */
    private String embedding;

    /**
     * 分块字符数
     */
    private Integer charCount;
}
