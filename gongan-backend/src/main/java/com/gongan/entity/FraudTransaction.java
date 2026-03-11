package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易流水实体
 */
@Data
@TableName("fraud_transaction")
public class FraudTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 人员ID
     */
    private Long customerId;

    /**
     * 账号
     */
    private String accountNo;

    /**
     * 对手账号
     */
    private String counterAccountNo;

    /**
     * 对手账户名
     */
    private String counterAccountName;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 交易渠道
     */
    private String channel;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 交易地点
     */
    private String location;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 风险标记
     */
    private Integer riskFlag;

    /**
     * 风险评分
     */
    private BigDecimal riskScore;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
