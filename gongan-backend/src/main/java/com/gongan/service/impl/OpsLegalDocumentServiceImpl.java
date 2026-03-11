package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.OpsLegalDocType;
import com.gongan.entity.OpsLegalDocument;
import com.gongan.exception.BusinessException;
import com.gongan.mapper.OpsLegalDocTypeMapper;
import com.gongan.mapper.OpsLegalDocumentMapper;
import com.gongan.service.OpsLegalDocumentService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 法律文书服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsLegalDocumentServiceImpl extends ServiceImpl<OpsLegalDocumentMapper, OpsLegalDocument> implements OpsLegalDocumentService {

    private final OpsLegalDocTypeMapper docTypeMapper;
    private final LLMClient llmClient;

    @Value("${file.pdf-path:./uploads/pdf}")
    private String pdfPath;

    @Override
    public PageResult<OpsLegalDocument> pageList(Map<String, Object> params) {
        int current = params.containsKey("current") ? (int) params.get("current") : 1;
        int size = params.containsKey("size") ? (int) params.get("size") : 10;

        LambdaQueryWrapper<OpsLegalDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpsLegalDocument::getDeleted, 0);

        if (params.containsKey("docTypeId")) {
            wrapper.eq(OpsLegalDocument::getDocTypeId, params.get("docTypeId"));
        }
        if (params.containsKey("status")) {
            wrapper.eq(OpsLegalDocument::getStatus, params.get("status"));
        }
        if (params.containsKey("docNo")) {
            wrapper.like(OpsLegalDocument::getDocNo, params.get("docNo"));
        }
        if (params.containsKey("docTitle")) {
            wrapper.like(OpsLegalDocument::getDocTitle, params.get("docTitle"));
        }

        wrapper.orderByDesc(OpsLegalDocument::getCreatedTime);

        IPage<OpsLegalDocument> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    @Transactional
    public OpsLegalDocument uploadDocument(MultipartFile file, Long docTypeId) {
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw BusinessException.of("只支持PDF文件上传");
        }

        // 生成文书编号
        String docNo = generateDocNo();

        // 保存文件
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = docNo + ".pdf";
        String filePath = pdfPath + "/" + datePath + "/" + fileName;

        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw BusinessException.of("文件保存失败");
        }

        // 创建文书记录
        OpsLegalDocument document = new OpsLegalDocument();
        document.setDocNo(docNo);
        document.setDocTypeId(docTypeId);
        document.setDocTitle(originalFilename.replace(".pdf", ""));
        document.setFileName(originalFilename);
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setStatus(0); // 待确认

        save(document);
        
        // 自动提取
        try {
            autoExtract(document);
        } catch (Exception e) {
            log.warn("自动提取失败，用户可手动提取", e);
        }

        return document;
    }
    
    /**
     * 自动提取PDF信息
     */
    private void autoExtract(OpsLegalDocument document) {
        // 提取PDF文本
        String pdfText = extractPdfText(document.getFilePath());
        
        if (pdfText == null || pdfText.isBlank()) {
            log.warn("PDF文本为空，无法提取");
            return;
        }
        
        // 调用大模型提取关键信息
        Map<String, Object> extractedData = extractWithLLM(pdfText);
        
        // 保存提取结果
        document.setExtractContent(JSON.toJSONString(extractedData));
        document.setAiConfidence((BigDecimal) extractedData.get("confidence"));
        
        // 提取特定字段到主表
        if (extractedData.get("amount") != null) {
            try {
                document.setAmount(new BigDecimal(extractedData.get("amount").toString()));
            } catch (Exception ignored) {}
        }
        if (extractedData.get("orgName") != null) {
            document.setOrgName(extractedData.get("orgName").toString());
        }
        
        updateById(document);
    }
    
    /**
     * 使用大模型提取关键信息
     */
    private Map<String, Object> extractWithLLM(String pdfText) {
        // 截取文本，避免过长
        String text = pdfText.length() > 4000 ? pdfText.substring(0, 4000) : pdfText;
        
        String prompt = """
            你是一个专业的法律文书信息提取助手。请从以下法律文书内容中提取关键信息。
            
            需要提取的字段：
            - orgName: 涉及机构名称（如银行、公司等）
            - userName: 涉及用户/当事人姓名
            - amount: 涉及金额（纯数字，不带单位）
            - currency: 币种（CNY/USD/EUR等）
            - caseNumber: 案号或文书编号
            - caseReason: 案由或事由
            - caseDate: 文书日期（YYYY-MM-DD格式）
            - court: 审理机构/发文单位
            - result: 判决结果或处理结果
            - riskLevel: 风险等级（HIGH/MEDIUM/LOW）
            
            请以JSON格式返回，如果某字段无法提取则返回null。格式示例：
            {"orgName": "XX银行", "userName": "张三", "amount": 100000, ...}
            
            法律文书内容：
            """ + text;
        
        String aiResponse = llmClient.chat(List.of(
                Map.of("role", "user", "content", prompt)
        ));
        
        // 解析AI响应
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```json") + 7, aiResponse.lastIndexOf("```"));
            } else if (aiResponse.contains("```")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```") + 3, aiResponse.lastIndexOf("```"));
            }
            JSONObject json = JSON.parseObject(jsonStr.trim());
            
            // 转换为Map
            result.put("orgName", json.getString("orgName"));
            result.put("userName", json.getString("userName"));
            result.put("amount", json.get("amount"));
            result.put("currency", json.getString("currency"));
            result.put("caseNumber", json.getString("caseNumber"));
            result.put("caseReason", json.getString("caseReason"));
            result.put("caseDate", json.getString("caseDate"));
            result.put("court", json.getString("court"));
            result.put("result", json.getString("result"));
            result.put("riskLevel", json.getString("riskLevel"));
            result.put("confidence", BigDecimal.valueOf(85)); // 默认置信度
            
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", aiResponse, e);
            // 返回空结果
            result.put("confidence", BigDecimal.ZERO);
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> extractWithAI(Long documentId) {
        OpsLegalDocument document = getById(documentId);
        if (document == null) {
            throw BusinessException.of("文书不存在");
        }

        // 提取PDF文本
        String pdfText = extractPdfText(document.getFilePath());
        
        if (pdfText == null || pdfText.isBlank()) {
            throw BusinessException.of("PDF文本提取失败");
        }

        // 调用大模型提取关键信息
        Map<String, Object> extractedData = extractWithLLM(pdfText);

        // 保存提取结果
        document.setExtractContent(JSON.toJSONString(extractedData));
        document.setAiConfidence((BigDecimal) extractedData.get("confidence"));
        
        // 提取特定字段到主表
        if (extractedData.get("amount") != null) {
            try {
                document.setAmount(new BigDecimal(extractedData.get("amount").toString()));
            } catch (Exception ignored) {}
        }
        if (extractedData.get("orgName") != null) {
            document.setOrgName((String) extractedData.get("orgName"));
        }
        
        updateById(document);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("documentId", documentId);
        result.put("extractedData", extractedData);
        result.put("confidence", extractedData.get("confidence"));
        result.put("pdfText", pdfText.length() > 500 ? pdfText.substring(0, 500) + "..." : pdfText);

        return result;
    }

    @Override
    @Transactional
    public void confirmExtract(Long documentId, Map<String, Object> confirmedData) {
        OpsLegalDocument document = getById(documentId);
        if (document == null) {
            throw BusinessException.of("文书不存在");
        }

        // 更新确认数据
        document.setExtractContent(JSON.toJSONString(confirmedData));
        document.setStatus(1); // 已确认
        document.setConfirmTime(LocalDateTime.now());

        // 提取特定字段到主表
        if (confirmedData.containsKey("amount")) {
            Object amountObj = confirmedData.get("amount");
            if (amountObj != null) {
                document.setAmount(new BigDecimal(amountObj.toString()));
            }
        }
        if (confirmedData.containsKey("org_name")) {
            document.setOrgName((String) confirmedData.get("org_name"));
        }

        updateById(document);
    }

    @Override
    @Transactional
    public void rejectExtract(Long documentId, String reason) {
        OpsLegalDocument document = getById(documentId);
        if (document == null) {
            throw BusinessException.of("文书不存在");
        }

        document.setStatus(2); // 已驳回
        document.setRemark(reason);
        updateById(document);
    }

    /**
     * 提取PDF文本
     */
    private String extractPdfText(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("PDF文本提取失败", e);
            throw BusinessException.of("PDF文本提取失败");
        }
    }

    /**
     * 生成文书编号
     */
    private String generateDocNo() {
        String prefix = "DOC";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%03d", new Random().nextInt(1000));
        return prefix + dateStr + randomStr;
    }
}
