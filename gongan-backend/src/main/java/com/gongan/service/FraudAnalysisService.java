package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.FraudCaseAnalysis;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.entity.FraudTransaction;

import java.util.List;
import java.util.Map;

/**
 * 反欺诈服务接口
 */
public interface FraudAnalysisService extends IService<FraudCaseAnalysis> {

    /**
     * 分页查询可疑人员
     */
    PageResult<FraudSuspiciousCustomer> pageCustomers(Map<String, Object> params);

    /**
     * 分页查询交易流水
     */
    PageResult<FraudTransaction> pageTransactions(Map<String, Object> params);

    /**
     * AI分析欺诈案例（创建新案例）
     */
    Map<String, Object> analyzeCase(Long customerId);

    /**
     * 重新分析（覆盖已有案例的结果）
     */
    Map<String, Object> reanalyzeCase(Long caseId);

    /**
     * 生成分析报告
     */
    String generateReport(Long caseId);

    /**
     * 查询人员的历史案例分析记录
     */
    List<FraudCaseAnalysis> listByCustomerId(Long customerId);

    /**
     * 获取最新案例
     */
    FraudCaseAnalysis getLatestByCustomerId(Long customerId);
}
