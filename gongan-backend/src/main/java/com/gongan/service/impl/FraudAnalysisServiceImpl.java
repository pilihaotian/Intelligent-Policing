package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.FraudCaseAnalysis;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.entity.FraudTransaction;
import com.gongan.exception.BusinessException;
import com.gongan.mapper.FraudCaseAnalysisMapper;
import com.gongan.mapper.FraudSuspiciousCustomerMapper;
import com.gongan.mapper.FraudTransactionMapper;
import com.gongan.service.FraudAnalysisService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 反欺诈服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudAnalysisServiceImpl extends ServiceImpl<FraudCaseAnalysisMapper, FraudCaseAnalysis> implements FraudAnalysisService {

    private final FraudSuspiciousCustomerMapper customerMapper;
    private final FraudTransactionMapper transactionMapper;
    private final LLMClient llmClient;

    @Override
    public PageResult<FraudSuspiciousCustomer> pageCustomers(Map<String, Object> params) {
        int current = (int) params.getOrDefault("current", 1);
        int size = (int) params.getOrDefault("size", 10);

        LambdaQueryWrapper<FraudSuspiciousCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FraudSuspiciousCustomer::getDeleted, 0);

        if (params.containsKey("customerName")) {
            wrapper.like(FraudSuspiciousCustomer::getCustomerName, params.get("customerName"));
        }
        if (params.containsKey("riskLevel")) {
            wrapper.eq(FraudSuspiciousCustomer::getRiskLevel, params.get("riskLevel"));
        }
        if (params.containsKey("status")) {
            wrapper.eq(FraudSuspiciousCustomer::getStatus, params.get("status"));
        }

        wrapper.orderByDesc(FraudSuspiciousCustomer::getRiskScore);

        Page<FraudSuspiciousCustomer> page = customerMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<FraudTransaction> pageTransactions(Map<String, Object> params) {
        int current = (int) params.getOrDefault("current", 1);
        int size = (int) params.getOrDefault("size", 10);

        LambdaQueryWrapper<FraudTransaction> wrapper = new LambdaQueryWrapper<>();

        if (params.containsKey("customerId")) {
            wrapper.eq(FraudTransaction::getCustomerId, params.get("customerId"));
        }
        if (params.containsKey("riskFlag")) {
            wrapper.eq(FraudTransaction::getRiskFlag, params.get("riskFlag"));
        }
        if (params.containsKey("startTime")) {
            wrapper.ge(FraudTransaction::getTransactionTime, params.get("startTime"));
        }
        if (params.containsKey("endTime")) {
            wrapper.le(FraudTransaction::getTransactionTime, params.get("endTime"));
        }

        wrapper.orderByDesc(FraudTransaction::getTransactionTime);

        Page<FraudTransaction> page = transactionMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    @Transactional
    public Map<String, Object> analyzeCase(Long customerId) {
        // 获取人员信息
        FraudSuspiciousCustomer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw BusinessException.of("人员不存在");
        }

        // 获取交易流水
        List<FraudTransaction> transactions = getTransactionsByCustomerId(customerId);

        // 调用AI分析
        String aiResponse = callAiAnalysis(customer, transactions);

        // 解析AI响应
        JSONObject analysisResult = parseAiResponse(aiResponse);

        // 保存案例
        FraudCaseAnalysis caseAnalysis = new FraudCaseAnalysis();
        caseAnalysis.setCaseNo("CA" + System.currentTimeMillis());
        caseAnalysis.setCustomerId(customerId);
        caseAnalysis.setCaseType(customer.getSuspiciousType());
        caseAnalysis.setCaseSource("SYSTEM");
        caseAnalysis.setSuspiciousPoints(analysisResult.getJSONArray("suspicious_points").toJSONString());
        caseAnalysis.setAnalysisContent(aiResponse);
        caseAnalysis.setConfidence(analysisResult.getBigDecimal("confidence"));
        caseAnalysis.setStatus(0); // 待处理

        save(caseAnalysis);

        // 返回结果
        return buildResult(caseAnalysis, analysisResult, customer, transactions.size());
    }

    @Override
    @Transactional
    public Map<String, Object> reanalyzeCase(Long caseId) {
        // 获取已有案例
        FraudCaseAnalysis caseAnalysis = getById(caseId);
        if (caseAnalysis == null) {
            throw BusinessException.of("案例不存在");
        }

        // 获取人员信息
        FraudSuspiciousCustomer customer = customerMapper.selectById(caseAnalysis.getCustomerId());
        if (customer == null) {
            throw BusinessException.of("人员不存在");
        }

        // 获取交易流水
        List<FraudTransaction> transactions = getTransactionsByCustomerId(caseAnalysis.getCustomerId());

        // 调用AI分析
        String aiResponse = callAiAnalysis(customer, transactions);

        // 解析AI响应
        JSONObject analysisResult = parseAiResponse(aiResponse);

        // 更新案例（覆盖旧结果）
        caseAnalysis.setSuspiciousPoints(analysisResult.getJSONArray("suspicious_points").toJSONString());
        caseAnalysis.setAnalysisContent(aiResponse);
        caseAnalysis.setConfidence(analysisResult.getBigDecimal("confidence"));
        caseAnalysis.setAnalysisReport(null); // 清空旧报告
        caseAnalysis.setUpdatedTime(LocalDateTime.now());

        updateById(caseAnalysis);

        // 返回结果
        return buildResult(caseAnalysis, analysisResult, customer, transactions.size());
    }

    @Override
    public String generateReport(Long caseId) {
        FraudCaseAnalysis caseAnalysis = getById(caseId);
        if (caseAnalysis == null) {
            throw BusinessException.of("案例不存在");
        }

        FraudSuspiciousCustomer customer = customerMapper.selectById(caseAnalysis.getCustomerId());

        // 构建报告生成提示词
        String systemPrompt = """
                你是一个专业的报告撰写专家，请根据提供的案例分析结果，生成一份格式规范、内容详实的反欺诈分析报告。
                
                报告应包含：
                1. 案例概述
                2. 人员基本信息
                3. 风险疑点分析
                4. 交易行为分析
                5. 风险评估结论
                6. 处置建议
                
                请使用Markdown格式输出。
                """;

        String userPrompt = String.format("""
                案例编号：%s
                案例类型：%s
                
                分析结果：
                %s
                """, caseAnalysis.getCaseNo(), caseAnalysis.getCaseType(), caseAnalysis.getAnalysisContent());

        String report = llmClient.chat(List.of(
                Map.of("role", "user", "content", userPrompt)
        ), systemPrompt);

        // 保存报告
        caseAnalysis.setAnalysisReport(report);
        caseAnalysis.setUpdatedTime(LocalDateTime.now());
        updateById(caseAnalysis);

        return report;
    }

    @Override
    public List<FraudCaseAnalysis> listByCustomerId(Long customerId) {
        LambdaQueryWrapper<FraudCaseAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FraudCaseAnalysis::getCustomerId, customerId)
                .eq(FraudCaseAnalysis::getDeleted, 0)
                .orderByDesc(FraudCaseAnalysis::getCreatedTime);
        return list(wrapper);
    }

    @Override
    public FraudCaseAnalysis getLatestByCustomerId(Long customerId) {
        LambdaQueryWrapper<FraudCaseAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FraudCaseAnalysis::getCustomerId, customerId)
                .eq(FraudCaseAnalysis::getDeleted, 0)
                .orderByDesc(FraudCaseAnalysis::getCreatedTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 获取人员的交易流水
     */
    private List<FraudTransaction> getTransactionsByCustomerId(Long customerId) {
        LambdaQueryWrapper<FraudTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FraudTransaction::getCustomerId, customerId)
                .orderByDesc(FraudTransaction::getTransactionTime)
                .last("LIMIT 50");
        return transactionMapper.selectList(wrapper);
    }

    /**
     * 调用AI分析
     */
    private String callAiAnalysis(FraudSuspiciousCustomer customer, List<FraudTransaction> transactions) {
        // 构建分析数据
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("人员信息：\n");
        dataBuilder.append(String.format("姓名：%s，人员号：%s\n", customer.getCustomerName(), customer.getCustomerNo()));
        dataBuilder.append(String.format("风险等级：%s，风险评分：%s\n", customer.getRiskLevel(), customer.getRiskScore()));
        dataBuilder.append(String.format("可疑类型：%s，可疑次数：%d\n\n", customer.getSuspiciousType(), customer.getSuspiciousCount()));

        dataBuilder.append("最近交易记录（最多50条）：\n");
        for (FraudTransaction trans : transactions) {
            dataBuilder.append(String.format("- %s | %s | %.2f元 | %s\n",
                    trans.getTransactionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    trans.getTransactionType(),
                    trans.getAmount(),
                    trans.getChannel()));
        }

        // AI分析提示词
        String systemPrompt = """
                你是一个专业的反欺诈分析师，负责分析可疑人员和交易数据，识别潜在的欺诈风险。
                
                请分析提供的数据，输出以下内容：
                1. 风险疑点分析：列出发现的可疑点，每个疑点包含描述和严重程度
                2. 行为模式分析：分析人员的交易行为模式
                3. 风险结论：综合评估欺诈可能性
                4. 建议措施：针对发现的问题提出处理建议
                
                请以JSON格式返回：
                {
                    "suspicious_points": [
                        {"point": "疑点描述", "severity": "高/中/低", "evidence": "证据描述"}
                    ],
                    "behavior_pattern": "行为模式分析",
                    "risk_conclusion": "风险结论",
                    "suggestions": ["建议1", "建议2"],
                    "confidence": 置信度(0-100)
                }
                """;

        return llmClient.chat(List.of(
                Map.of("role", "user", "content", dataBuilder.toString())
        ), systemPrompt);
    }

    /**
     * 解析AI响应
     */
    private JSONObject parseAiResponse(String aiResponse) {
        try {
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```json") + 7, aiResponse.lastIndexOf("```"));
            } else if (aiResponse.contains("```")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```") + 3, aiResponse.lastIndexOf("```"));
            }
            return JSON.parseObject(jsonStr.trim());
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            throw BusinessException.of("AI分析失败，请稍后重试");
        }
    }

    /**
     * 构建返回结果
     */
    private Map<String, Object> buildResult(FraudCaseAnalysis caseAnalysis, JSONObject analysisResult,
                                            FraudSuspiciousCustomer customer, int transactionCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("caseId", caseAnalysis.getId());
        result.put("caseNo", caseAnalysis.getCaseNo());
        result.put("analysisResult", analysisResult);
        result.put("customer", customer);
        result.put("transactionCount", transactionCount);
        result.put("analysisReport", caseAnalysis.getAnalysisReport());
        return result;
    }
}
