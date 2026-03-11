package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.AmlCustomerDueDiligence;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.entity.FraudTransaction;
import com.gongan.exception.BusinessException;
import com.gongan.mapper.AmlCustomerDueDiligenceMapper;
import com.gongan.mapper.FraudSuspiciousCustomerMapper;
import com.gongan.mapper.FraudTransactionMapper;
import com.gongan.service.AmlService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 反洗钱服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmlServiceImpl extends ServiceImpl<AmlCustomerDueDiligenceMapper, AmlCustomerDueDiligence> implements AmlService {

    private final FraudSuspiciousCustomerMapper customerMapper;
    private final FraudTransactionMapper transactionMapper;
    private final LLMClient llmClient;

    @Override
    public PageResult<AmlCustomerDueDiligence> pageDueDiligence(Map<String, Object> params) {
        int current = (int) params.getOrDefault("current", 1);
        int size = (int) params.getOrDefault("size", 10);

        LambdaQueryWrapper<AmlCustomerDueDiligence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AmlCustomerDueDiligence::getDeleted, 0);

        if (params.containsKey("customerId")) {
            wrapper.eq(AmlCustomerDueDiligence::getCustomerId, params.get("customerId"));
        }
        if (params.containsKey("ddType")) {
            wrapper.eq(AmlCustomerDueDiligence::getDdType, params.get("ddType"));
        }
        if (params.containsKey("status")) {
            wrapper.eq(AmlCustomerDueDiligence::getStatus, params.get("status"));
        }

        wrapper.orderByDesc(AmlCustomerDueDiligence::getCreatedTime);

        Page<AmlCustomerDueDiligence> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    @Transactional
    public Map<String, Object> aiAnalyzeDueDiligence(Long ddId) {
        AmlCustomerDueDiligence dd = getById(ddId);
        if (dd == null) {
            throw BusinessException.of("尽调记录不存在");
        }

        // 获取人员信息
        FraudSuspiciousCustomer customer = customerMapper.selectById(dd.getCustomerId());

        // 构建AI分析提示词
        String systemPrompt = """
                你是一个专业的公安情报分析员，负责分析人员背景核查信息，识别潜在的安全风险。
                
                请分析提供的数据，输出以下内容：
                1. 人员风险评估：分析人员背景、活动轨迹等风险因素
                2. 关联人员分析：分析关联关系和可疑联系
                3. 活动来源分析：评估活动来源的合理性
                4. 可疑点识别：列出发现的可疑点
                5. 核查建议：提出后续核查措施建议
                
                请以JSON格式返回：
                {
                    "risk_assessment": {
                        "level": "低/中/高/极高",
                        "factors": ["风险因素1", "风险因素2"]
                    },
                    "suspicious_points": [
                        {"point": "可疑点描述", "severity": "高/中/低"}
                    ],
                    "suggestions": ["建议1", "建议2"],
                    "confidence": 置信度(0-100)
                }
                """;

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("人员尽调信息：\n");
        userPromptBuilder.append(String.format("尽调类型：%s\n", dd.getDdType()));
        userPromptBuilder.append(String.format("尽调原因：%s\n", dd.getDdReason()));
        userPromptBuilder.append(String.format("业务性质：%s\n", dd.getBusinessNature()));
        userPromptBuilder.append(String.format("资金来源：%s\n", dd.getFundSource()));
        
        if (dd.getCustomerInfo() != null) {
            userPromptBuilder.append(String.format("人员信息：%s\n", dd.getCustomerInfo()));
        }
        if (dd.getBeneficialOwner() != null) {
            userPromptBuilder.append(String.format("受益所有人：%s\n", dd.getBeneficialOwner()));
        }

        String aiResponse = llmClient.chat(List.of(
                Map.of("role", "user", "content", userPromptBuilder.toString())
        ), systemPrompt);

        // 解析结果
        JSONObject analysisResult;
        try {
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```json") + 7, aiResponse.lastIndexOf("```"));
            } else if (aiResponse.contains("```")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```") + 3, aiResponse.lastIndexOf("```"));
            }
            analysisResult = JSON.parseObject(jsonStr.trim());
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            throw BusinessException.of("AI分析失败，请稍后重试");
        }

        // 保存分析结果
        dd.setAiAnalysis(aiResponse);
        dd.setAiSuggestions(analysisResult.getJSONArray("suggestions").toJSONString());
        updateById(dd);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("ddId", ddId);
        result.put("analysisResult", analysisResult);
        result.put("customer", customer);

        return result;
    }

    @Override
    public Map<String, Object> suspiciousTransactionScreening(Long customerId) {
        // 获取人员信息
        FraudSuspiciousCustomer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw BusinessException.of("人员不存在");
        }

        // 获取交易流水
        LambdaQueryWrapper<FraudTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FraudTransaction::getCustomerId, customerId)
                .orderByDesc(FraudTransaction::getTransactionTime)
                .last("LIMIT 100");
        List<FraudTransaction> transactions = transactionMapper.selectList(wrapper);

        // 构建甄别提示词
        String systemPrompt = """
                你是一个专业的反洗钱交易甄别专家，负责分析人员交易数据，识别可疑交易模式。
                
                请分析提供的数据，输出以下内容：
                1. 交易模式分析：分析交易的时间、金额、频率等特征
                2. 可疑交易识别：识别可疑的交易行为
                3. 风险特征：总结洗钱风险特征
                4. 甄别结论：给出是否可疑的判断
                5. 建议措施：提出后续处理建议
                
                请以JSON格式返回。
                """;

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append(String.format("人员：%s（%s），风险等级：%s\n\n",
                customer.getCustomerName(), customer.getCustomerNo(), customer.getRiskLevel()));
        userPromptBuilder.append(String.format("交易记录共%d条：\n", transactions.size()));

        // 汇总交易数据
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", transactions.size());
        summary.put("totalAmount", transactions.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum());
        summary.put("transferCount", transactions.stream().filter(t -> "TRANSFER".equals(t.getTransactionType())).count());
        summary.put("withdrawCount", transactions.stream().filter(t -> "WITHDRAW".equals(t.getTransactionType())).count());

        userPromptBuilder.append(String.format("交易汇总：%s\n", JSON.toJSONString(summary)));

        String aiResponse = llmClient.chat(List.of(
                Map.of("role", "user", "content", userPromptBuilder.toString())
        ), systemPrompt);

        Map<String, Object> result = new HashMap<>();
        result.put("customerId", customerId);
        result.put("customer", customer);
        result.put("transactionSummary", summary);
        result.put("analysisResult", aiResponse);

        return result;
    }

    @Override
    public String generateSuspiciousReport(Long reportId) {
        // 实际项目中应从数据库获取报告信息并生成报告
        // 这里简化处理
        return "可疑交易报告生成功能待完善";
    }

    @Override
    public PageResult<Map<String, Object>> pageSuspiciousList(Integer current, Integer size, String customerName, String status) {
        // 查询可疑人员列表作为可疑交易列表
        LambdaQueryWrapper<FraudSuspiciousCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FraudSuspiciousCustomer::getDeleted, 0);
        
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like(FraudSuspiciousCustomer::getCustomerName, customerName);
        }
        if (status != null && !status.isEmpty()) {
            // status 映射到风险等级
            wrapper.eq(FraudSuspiciousCustomer::getRiskLevel, status);
        }
        
        wrapper.orderByDesc(FraudSuspiciousCustomer::getCreatedTime);
        
        Page<FraudSuspiciousCustomer> page = customerMapper.selectPage(new Page<>(current, size), wrapper);
        
        // 转换为Map格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (FraudSuspiciousCustomer customer : page.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", customer.getId());
            map.put("customerName", customer.getCustomerName());
            map.put("idCard", customer.getIdNo());
            map.put("transactionCount", customer.getSuspiciousCount() != null ? customer.getSuspiciousCount() : 0);
            map.put("totalAmount", 0);
            map.put("alertType", "大额可疑交易");
            map.put("status", "PENDING");
            map.put("createTime", customer.getCreatedTime());
            records.add(map);
        }
        
        return PageResult.of(records, page.getTotal(), current, size);
    }
}
