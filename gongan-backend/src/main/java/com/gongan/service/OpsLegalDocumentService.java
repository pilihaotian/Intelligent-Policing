package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.OpsLegalDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 法律文书服务接口
 */
public interface OpsLegalDocumentService extends IService<OpsLegalDocument> {

    /**
     * 分页查询
     */
    PageResult<OpsLegalDocument> pageList(Map<String, Object> params);

    /**
     * 上传文书
     */
    OpsLegalDocument uploadDocument(MultipartFile file, Long docTypeId);

    /**
     * AI智能提取
     */
    Map<String, Object> extractWithAI(Long documentId);

    /**
     * 确认提取结果
     */
    void confirmExtract(Long documentId, Map<String, Object> confirmedData);

    /**
     * 驳回提取结果
     */
    void rejectExtract(Long documentId, String reason);
}
