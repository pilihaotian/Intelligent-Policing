package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.AiKnowledgeBase;
import com.gongan.entity.AiKnowledgeDocument;
import com.gongan.filter.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * AI智能问答服务接口
 */
public interface AiChatService extends IService<AiKnowledgeBase> {

    PageResult<AiKnowledgeBase> pageList(Map<String, Object> params);

    AiKnowledgeBase createKnowledgeBase(AiKnowledgeBase kb);

    AiKnowledgeDocument uploadDocument(Long kbId, MultipartFile file);

    Map<String, Object> chat(Long kbId, String sessionId, String question, UserPrincipal user);

    List<AiKnowledgeDocument> listDocuments(Long kbId);

    void deleteDocument(Long docId);
}
