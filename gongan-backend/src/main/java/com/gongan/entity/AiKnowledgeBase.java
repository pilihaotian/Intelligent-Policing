package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_knowledge_base")
public class AiKnowledgeBase extends BaseEntity {

    /**
     * 知识库编码
     */
    private String kbCode;

    /**
     * 知识库名称
     */
    private String kbName;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 知识库分类
     */
    private String kbType;

    /**
     * 文档数量
     */
    private Integer docCount;

    /**
     * 状态
     */
    private Integer status;
}
