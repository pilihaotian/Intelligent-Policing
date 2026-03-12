package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线索报告实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("aml_suspicious_transaction_report")
public class AmlSuspiciousTransactionReport extends BaseEntity {

    /**
     * 线索编号
     */
    private String reportNo;

    /**
     * 人员ID
     */
    private Long customerId;

    /**
     * 人员姓名
     */
    private String customerName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 涉及交易笔数
     */
    private Integer transactionCount;

    /**
     * 涉及总金额
     */
    private BigDecimal totalAmount;

    /**
     * 线索类型
     */
    private String alertType;

    /**
     * 可疑类型
     */
    private String suspiciousTypes;

    /**
     * AI分析结果
     */
    private String analysisResult;

    /**
     * 分析报告内容
     */
    private String reportContent;

    /**
     * 状态
     */
    private String status;

    /**
     * 报告人ID
     */
    private Long reporterId;

    /**
     * 报告时间
     */
    private LocalDateTime reportTime;
}
