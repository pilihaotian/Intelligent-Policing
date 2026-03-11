package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.FraudCaseAnalysis;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.entity.FraudTransaction;
import com.gongan.service.FraudAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反欺诈控制器
 */
@Tag(name = "反欺诈管理")
@RestController
@RequestMapping("/fraud")
@RequiredArgsConstructor
public class FraudAnalysisController {

    private final FraudAnalysisService fraudAnalysisService;

    @Operation(summary = "可疑人员列表")
    @GetMapping("/customer/list")
    public ApiResponse<PageResult<FraudSuspiciousCustomer>> listCustomers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) Integer status) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (customerName != null) params.put("customerName", customerName);
        if (riskLevel != null) params.put("riskLevel", riskLevel);
        if (status != null) params.put("status", status);

        return ApiResponse.success(fraudAnalysisService.pageCustomers(params));
    }

    @Operation(summary = "交易流水列表")
    @GetMapping("/transaction/list")
    public ApiResponse<PageResult<FraudTransaction>> listTransactions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer riskFlag,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (customerId != null) params.put("customerId", customerId);
        if (riskFlag != null) params.put("riskFlag", riskFlag);
        if (startTime != null) params.put("startTime", startTime);
        if (endTime != null) params.put("endTime", endTime);

        return ApiResponse.success(fraudAnalysisService.pageTransactions(params));
    }

    @Operation(summary = "AI分析案例（创建新分析）")
    @PostMapping("/analysis")
    public ApiResponse<Map<String, Object>> analyzeCase(@RequestParam Long customerId) {
        Map<String, Object> result = fraudAnalysisService.analyzeCase(customerId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "重新分析（覆盖已有案例结果）")
    @PostMapping("/analysis/reanalyze/{caseId}")
    public ApiResponse<Map<String, Object>> reanalyzeCase(@PathVariable Long caseId) {
        Map<String, Object> result = fraudAnalysisService.reanalyzeCase(caseId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "生成分析报告")
    @PostMapping("/report/generate/{caseId}")
    public ApiResponse<String> generateReport(@PathVariable Long caseId) {
        String report = fraudAnalysisService.generateReport(caseId);
        return ApiResponse.success(report);
    }

    @Operation(summary = "获取案例详情")
    @GetMapping("/case/{id}")
    public ApiResponse<FraudCaseAnalysis> getCase(@PathVariable Long id) {
        return ApiResponse.success(fraudAnalysisService.getById(id));
    }

    @Operation(summary = "查询人员的历史案例分析")
    @GetMapping("/case/list/{customerId}")
    public ApiResponse<List<FraudCaseAnalysis>> listCasesByCustomer(@PathVariable Long customerId) {
        return ApiResponse.success(fraudAnalysisService.listByCustomerId(customerId));
    }

    @Operation(summary = "获取人员最新的案例分析")
    @GetMapping("/case/latest/{customerId}")
    public ApiResponse<FraudCaseAnalysis> getLatestCase(@PathVariable Long customerId) {
        return ApiResponse.success(fraudAnalysisService.getLatestByCustomerId(customerId));
    }
}
