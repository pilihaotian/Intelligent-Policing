package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.AiKnowledgeBase;
import com.gongan.entity.AiKnowledgeDocument;
import com.gongan.exception.BusinessException;
import com.gongan.filter.UserPrincipal;
import com.gongan.mapper.AiKnowledgeBaseMapper;
import com.gongan.mapper.AiKnowledgeDocumentMapper;
import com.gongan.service.AiChatService;
import com.gongan.service.RagService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI智能问答服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl extends ServiceImpl<AiKnowledgeBaseMapper, AiKnowledgeBase> implements AiChatService {

    private final AiKnowledgeDocumentMapper documentMapper;
    private final LLMClient llmClient;
    private final RagService ragService;

    @Value("${file.knowledge-path:./uploads/knowledge}")
    private String knowledgePath;

    @Override
    public PageResult<AiKnowledgeBase> pageList(Map<String, Object> params) {
        int current = (int) params.getOrDefault("current", 1);
        int size = (int) params.getOrDefault("size", 10);

        LambdaQueryWrapper<AiKnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeBase::getDeleted, 0);

        if (params.containsKey("kbName")) {
            wrapper.like(AiKnowledgeBase::getKbName, params.get("kbName"));
        }
        if (params.containsKey("kbType")) {
            wrapper.eq(AiKnowledgeBase::getKbType, params.get("kbType"));
        }

        wrapper.orderByDesc(AiKnowledgeBase::getCreatedTime);

        var page = page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public AiKnowledgeBase createKnowledgeBase(AiKnowledgeBase kb) {
        kb.setDocCount(0);
        save(kb);
        return kb;
    }

    @Override
    public AiKnowledgeDocument uploadDocument(Long kbId, MultipartFile file) {
        AiKnowledgeBase kb = getById(kbId);
        if (kb == null) {
            throw BusinessException.of("知识库不存在");
        }

        String originalFilename = file.getOriginalFilename();
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = UUID.randomUUID().toString() + getFileExtension(originalFilename);
        String filePath = knowledgePath + "/" + datePath + "/" + fileName;

        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (Exception e) {
            log.error("文件保存失败", e);
            throw BusinessException.of("文件保存失败");
        }

        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setKbId(kbId);
        document.setDocName(originalFilename);
        document.setDocType(getFileType(originalFilename));
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setChunkCount(0);
        document.setStatus("PENDING");

        documentMapper.insert(document);

        // 更新知识库文档数量
        kb.setDocCount(kb.getDocCount() + 1);
        updateById(kb);

        // 异步处理文档（解析、分块、向量化）
        processDocumentAsync(document);

        return document;
    }

    /**
     * 异步处理文档
     */
    private void processDocumentAsync(AiKnowledgeDocument document) {
        new Thread(() -> {
            try {
                ragService.processDocument(document);
            } catch (Exception e) {
                log.error("文档处理失败，文档ID: {}", document.getId(), e);
            }
        }).start();
    }

    @Override
    public Map<String, Object> chat(Long kbId, String sessionId, String question, UserPrincipal user) {
        Map<String, Object> result;

        // 如果指定了知识库，使用RAG检索
        if (kbId != null && kbId > 0) {
            result = ragService.query(kbId, question);
        } else {
            // 无知识库时使用普通对话
            StringBuilder systemPrompt = new StringBuilder();
                    systemPrompt.append("你是一个专业的公安业务智能助手。");
                    systemPrompt.append("请根据用户的问题，提供专业、准确的回答。");
                    systemPrompt.append("如果问题涉及公安业务的专业知识，请结合相关法规和实践经验进行解答。");
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", question));

            String answer = llmClient.chat(messages, systemPrompt.toString());

            result = new HashMap<>();
            result.put("answer", answer);
            result.put("sources", new ArrayList<>());
        }

        result.put("sessionId", sessionId);
        return result;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    private String getFileType(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        return switch (ext) {
            case ".pdf" -> "pdf";
            case ".doc", ".docx" -> "word";
            case ".txt" -> "txt";
            default -> "other";
        };
    }

    @Override
    public List<AiKnowledgeDocument> listDocuments(Long kbId) {
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDocument::getKbId, kbId);
        wrapper.eq(AiKnowledgeDocument::getDeleted, 0);
        wrapper.orderByDesc(AiKnowledgeDocument::getCreatedTime);
        return documentMapper.selectList(wrapper);
    }

    @Override
    public void deleteDocument(Long docId) {
        AiKnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc != null) {
            documentMapper.deleteById(docId);
            // 更新知识库文档数量
            AiKnowledgeBase kb = getById(doc.getKbId());
            if (kb != null && kb.getDocCount() > 0) {
                kb.setDocCount(kb.getDocCount() - 1);
                updateById(kb);
            }
        }
    }
}