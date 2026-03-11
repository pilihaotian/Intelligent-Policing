package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.OpsLegalDocument;
import com.gongan.service.OpsLegalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 法律文书控制器
 */
@Tag(name = "法律文书管理")
@RestController
@RequestMapping("/ops/document")
@RequiredArgsConstructor
public class OpsLegalDocumentController {

    private final OpsLegalDocumentService documentService;

    @Operation(summary = "分页查询文书")
    @GetMapping("/list")
    public ApiResponse<PageResult<OpsLegalDocument>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long docTypeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) String docTitle) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (docTypeId != null) params.put("docTypeId", docTypeId);
        if (status != null) params.put("status", status);
        if (docNo != null) params.put("docNo", docNo);
        if (docTitle != null) params.put("docTitle", docTitle);

        return ApiResponse.success(documentService.pageList(params));
    }

    @Operation(summary = "获取文书详情")
    @GetMapping("/{id}")
    public ApiResponse<OpsLegalDocument> getById(@PathVariable Long id) {
        return ApiResponse.success(documentService.getById(id));
    }

    @Operation(summary = "上传文书")
    @PostMapping("/upload")
    public ApiResponse<OpsLegalDocument> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docTypeId", required = false) Long docTypeId) {
        OpsLegalDocument document = documentService.uploadDocument(file, docTypeId);
        return ApiResponse.success(document);
    }

    @Operation(summary = "AI智能提取")
    @PostMapping("/extract/{id}")
    public ApiResponse<Map<String, Object>> extract(@PathVariable Long id) {
        Map<String, Object> result = documentService.extractWithAI(id);
        return ApiResponse.success(result);
    }

    @Operation(summary = "确认提取结果")
    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(@RequestBody Map<String, Object> params) {
        Long documentId = Long.parseLong(params.get("documentId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> confirmedData = (Map<String, Object>) params.get("confirmedData");
        documentService.confirmExtract(documentId, confirmedData);
        return ApiResponse.success();
    }

    @Operation(summary = "驳回提取结果")
    @PostMapping("/reject/{id}")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        documentService.rejectExtract(id, reason);
        return ApiResponse.success();
    }

    @Operation(summary = "删除文书")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        documentService.removeById(id);
        return ApiResponse.success();
    }
}
