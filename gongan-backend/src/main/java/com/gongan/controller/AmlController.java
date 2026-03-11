package com.gongan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.AmlCustomerDueDiligence;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.service.AmlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反洗钱控制器
 */
@Tag(name = "反洗钱管理")
@RestController
@RequestMapping("/aml")
@RequiredArgsConstructor
public class AmlController {

    private final AmlService amlService;

    @Operation(summary = "人员尽调列表")
    @GetMapping("/dd/list")
    public ApiResponse<PageResult<AmlCustomerDueDiligence>> listDueDiligence(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String ddType,
            @RequestParam(required = false) Integer status) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (customerId != null) params.put("customerId", customerId);
        if (ddType != null) params.put("ddType", ddType);
        if (status != null) params.put("status", status);

        return ApiResponse.success(amlService.pageDueDiligence(params));
    }

    @Operation(summary = "获取尽调详情")
    @GetMapping("/dd/{id}")
    public ApiResponse<AmlCustomerDueDiligence> getDueDiligence(@PathVariable Long id) {
        return ApiResponse.success(amlService.getById(id));
    }

    @Operation(summary = "AI尽调分析")
    @PostMapping("/dd/analyze/{id}")
    public ApiResponse<Map<String, Object>> aiAnalyze(@PathVariable Long id) {
        Map<String, Object> result = amlService.aiAnalyzeDueDiligence(id);
        return ApiResponse.success(result);
    }

    @Operation(summary = "可疑交易列表")
    @GetMapping("/suspicious")
    public ApiResponse<PageResult<Map<String, Object>>> listSuspicious(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {
        
        PageResult<Map<String, Object>> result = amlService.pageSuspiciousList(current, size, customerName, status);
        return ApiResponse.success(result);
    }

    @Operation(summary = "可疑交易甄别")
    @PostMapping("/suspicious")
    public ApiResponse<Map<String, Object>> suspiciousScreening(@RequestParam Long customerId) {
        Map<String, Object> result = amlService.suspiciousTransactionScreening(customerId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "生成可疑报告")
    @PostMapping("/report/generate/{id}")
    public ApiResponse<String> generateReport(@PathVariable Long id) {
        String report = amlService.generateSuspiciousReport(id);
        return ApiResponse.success(report);
    }

    @Operation(summary = "新增尽调记录")
    @PostMapping("/dd")
    public ApiResponse<Void> addDueDiligence(@RequestBody AmlCustomerDueDiligence dd) {
        amlService.save(dd);
        return ApiResponse.success();
    }

    @Operation(summary = "更新尽调记录")
    @PutMapping("/dd")
    public ApiResponse<Void> updateDueDiligence(@RequestBody AmlCustomerDueDiligence dd) {
        amlService.updateById(dd);
        return ApiResponse.success();
    }
}
