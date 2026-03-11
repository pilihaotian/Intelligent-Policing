package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 法律文书实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_legal_document")
public class OpsLegalDocument extends BaseEntity {

    /**
     * 文书编号
     */
    private String docNo;

    /**
     * 文书类型ID
     */
    private Long docTypeId;

    /**
     * 文书标题
     */
    private String docTitle;

    /**
     * 原文件名
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 涉及机构名称
     */
    private String orgName;

    /**
     * 涉及金额
     */
    private BigDecimal amount;

    /**
     * 大模型提取内容(JSON)
     */
    private String extractContent;

    /**
     * AI提取置信度
     */
    private BigDecimal aiConfidence;

    /**
     * 状态: 0-待确认, 1-已确认, 2-已驳回
     */
    private Integer status;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 备注
     */
    private String remark;
}
