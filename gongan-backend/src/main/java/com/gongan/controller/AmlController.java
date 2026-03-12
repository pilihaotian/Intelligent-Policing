package com.gongan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.AmlCustomerDueDiligence;
import com.gongan.entity.AmlSuspiciousTransactionReport;
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
 * 治安管理控制器
 */
@Tag(name = "治安管理")
@RestController
@RequestMapping("/aml")
@RequiredArgsConstructor
public class AmlController {

    private final AmlService amlService;

    // ========== 人员核查相关接口 ==========

    @Operation(summary = "人员核查列表")
    @GetMapping("/dd/list")
    public ApiResponse<PageResult<AmlCustomerDueDiligence>> listDueDiligence(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String ddType,
            @RequestParam(required = false) String status) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (customerId != null) params.put("customerId", customerId);
        if (ddType != null) params.put("ddType", ddType);
        if (status != null) params.put("status", status);

        return ApiResponse.success(amlService.pageDueDiligence(params));
    }

    @Operation(summary = "获取核查详情")
    @GetMapping("/dd/{id}")
    public ApiResponse<AmlCustomerDueDiligence> getDueDiligence(@PathVariable Long id) {
        return ApiResponse.success(amlService.getById(id));
    }

    @Operation(summary = "AI辅助核查分析")
    @PostMapping("/dd/analyze/{id}")
    public ApiResponse<Map<String, Object>> aiAnalyze(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean force) {
        Map<String, Object> result = amlService.aiAnalyzeDueDiligence(id, force);
        return ApiResponse.success(result);
    }

    @Operation(summary = "新增核查记录")
    @PostMapping("/dd")
    public ApiResponse<Void> addDueDiligence(@RequestBody AmlCustomerDueDiligence dd) {
        amlService.save(dd);
        return ApiResponse.success();
    }

    @Operation(summary = "更新核查记录")
    @PutMapping("/dd")
    public ApiResponse<Void> updateDueDiligence(@RequestBody AmlCustomerDueDiligence dd) {
        amlService.updateById(dd);
        return ApiResponse.success();
    }

    // ========== 线索管理相关接口 ==========

    @Operation(summary = "线索列表")
    @GetMapping("/clue/list")
    public ApiResponse<PageResult<AmlSuspiciousTransactionReport>> listClues(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {
        
        PageResult<AmlSuspiciousTransactionReport> result = amlService.pageClueList(current, size, customerName, status);
        return ApiResponse.success(result);
    }

    @Operation(summary = "线索详情")
    @GetMapping("/clue/{id}")
    public ApiResponse<AmlSuspiciousTransactionReport> getClue(@PathVariable Long id) {
        return ApiResponse.success(amlService.getClueById(id));
    }

    @Operation(summary = "AI分析线索")
    @PostMapping("/clue/analyze/{id}")
    public ApiResponse<Map<String, Object>> aiAnalyzeClue(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean force) {
        Map<String, Object> result = amlService.aiAnalyzeClue(id, force);
        return ApiResponse.success(result);
    }

    @Operation(summary = "处理线索")
    @PostMapping("/clue/handle/{id}")
    public ApiResponse<Void> handleClue(
            @PathVariable Long id,
            @RequestParam String conclusion,
            @RequestParam(required = false) String description) {
        amlService.handleClue(id, conclusion, description);
        return ApiResponse.success();
    }

    @Operation(summary = "生成线索报告")
    @PostMapping("/clue/report/{id}")
    public ApiResponse<String> generateClueReport(@PathVariable Long id) {
        String report = amlService.generateClueReport(id);
        return ApiResponse.success(report);
    }

    // ========== 兼容旧接口 ==========

    @Operation(summary = "可疑交易列表（兼容）")
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
}
