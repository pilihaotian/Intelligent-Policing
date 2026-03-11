package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 人员尽调实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("aml_customer_due_diligence")
public class AmlCustomerDueDiligence extends BaseEntity {

    /**
     * 人员ID
     */
    private Long customerId;

    /**
     * 尽调类型
     */
    private String ddType;

    /**
     * 尽调原因
     */
    private String ddReason;

    /**
     * 人员信息(JSON)
     */
    private String customerInfo;

    /**
     * 受益所有人(JSON)
     */
    private String beneficialOwner;

    /**
     * 业务性质
     */
    private String businessNature;

    /**
     * 资金来源
     */
    private String fundSource;

    /**
     * 风险评估(JSON)
     */
    private String riskAssessment;

    /**
     * AI分析内容
     */
    private String aiAnalysis;

    /**
     * AI建议
     */
    private String aiSuggestions;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核结果
     */
    private String reviewResult;
}
