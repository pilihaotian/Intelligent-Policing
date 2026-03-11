package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识文档实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_knowledge_document")
public class AiKnowledgeDocument extends BaseEntity {

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 分块数量
     */
    private Integer chunkCount;

    /**
     * 状态: PENDING-待处理, PROCESSING-处理中, READY-就绪, FAILED-失败
     */
    private String status;
}
