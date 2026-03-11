package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 欺诈案例分析实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fraud_case_analysis")
public class FraudCaseAnalysis extends BaseEntity {

    /**
     * 案例编号
     */
    private String caseNo;

    /**
     * 人员ID
     */
    private Long customerId;

    /**
     * 案例类型
     */
    private String caseType;

    /**
     * 案例来源
     */
    private String caseSource;

    /**
     * AI分析的疑点(JSON数组)
     */
    private String suspiciousPoints;

    /**
     * AI分析内容
     */
    private String analysisContent;

    /**
     * 分析报告
     */
    private String analysisReport;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;
}
