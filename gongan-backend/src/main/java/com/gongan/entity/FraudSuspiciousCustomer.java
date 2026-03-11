package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 可疑人员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fraud_suspicious_customer")
public class FraudSuspiciousCustomer extends BaseEntity {

    /**
     * 人员编号
     */
    private String customerNo;

    /**
     * 人员姓名
     */
    private String customerName;

    /**
     * 证件类型
     */
    private String idType;

    /**
     * 证件号码
     */
    private String idNo;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 人员类型
     */
    private String customerType;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险评分
     */
    private BigDecimal riskScore;

    /**
     * 黑名单标记
     */
    private Integer blacklistFlag;

    /**
     * 关注名单标记
     */
    private Integer watchlistFlag;

    /**
     * 可疑类型
     */
    private String suspiciousType;

    /**
     * 首次可疑时间
     */
    private LocalDateTime firstSuspiciousTime;

    /**
     * 最近可疑时间
     */
    private LocalDateTime lastSuspiciousTime;

    /**
     * 可疑次数
     */
    private Integer suspiciousCount;

    /**
     * 状态
     */
    private Integer status;
}
