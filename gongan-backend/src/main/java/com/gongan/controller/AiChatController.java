package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.AiChatMessage;
import com.gongan.entity.AiChatSession;
import com.gongan.entity.AiKnowledgeBase;
import com.gongan.entity.AiKnowledgeDocument;
import com.gongan.filter.UserPrincipal;
import com.gongan.service.AiChatService;
import com.gongan.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI智能助手控制器
 */
@Tag(name = "智能助手")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService chatService;
    private final ChatSessionService sessionService;

    @Operation(summary = "知识库列表")
    @GetMapping("/kb/list")
    public ApiResponse<PageResult<AiKnowledgeBase>> listKb(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String kbName,
            @RequestParam(required = false) String kbType) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (kbName != null) params.put("kbName", kbName);
        if (kbType != null) params.put("kbType", kbType);

        return ApiResponse.success(chatService.pageList(params));
    }

    @Operation(summary = "创建知识库")
    @PostMapping("/kb")
    public ApiResponse<AiKnowledgeBase> createKb(@RequestBody AiKnowledgeBase kb) {
        return ApiResponse.success(chatService.createKnowledgeBase(kb));
    }

    @Operation(summary = "更新知识库")
    @PutMapping("/kb")
    public ApiResponse<Void> updateKb(@RequestBody AiKnowledgeBase kb) {
        chatService.updateById(kb);
        return ApiResponse.success();
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/kb/{id}")
    public ApiResponse<Void> deleteKb(@PathVariable Long id) {
        chatService.removeById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "获取知识库文档列表")
    @GetMapping("/kb/{kbId}/documents")
    public ApiResponse<List<AiKnowledgeDocument>> listDocuments(@PathVariable Long kbId) {
        return ApiResponse.success(chatService.listDocuments(kbId));
    }

    @Operation(summary = "获取文档列表")
    @GetMapping("/document/list")
    public ApiResponse<List<AiKnowledgeDocument>> listDocumentsByParam(@RequestParam Long kbId) {
        return ApiResponse.success(chatService.listDocuments(kbId));
    }

    @Operation(summary = "上传知识文档")
    @PostMapping("/kb/{kbId}/documents")
    public ApiResponse<AiKnowledgeDocument> uploadDoc(
            @PathVariable Long kbId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(chatService.uploadDocument(kbId, file));
    }

    @Operation(summary = "上传文档")
    @PostMapping("/document/upload")
    public ApiResponse<Void> uploadDocsByParam(
            @RequestParam Long kbId,
            @RequestParam("files") List<MultipartFile> files) {
        for (MultipartFile file : files) {
            chatService.uploadDocument(kbId, file);
        }
        return ApiResponse.success();
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/document/{id}")
    public ApiResponse<Void> deleteDoc(@PathVariable Long id) {
        chatService.deleteDocument(id);
        return ApiResponse.success();
    }

    // ==================== 会话管理接口 ====================

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public ApiResponse<List<AiChatSession>> listSessions(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(sessionService.listSessions(user.getUserId()));
    }

    @Operation(summary = "创建新会话")
    @PostMapping("/sessions")
    public ApiResponse<AiChatSession> createSession(
            @RequestParam(required = false) Long kbId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(sessionService.createSession(user.getUserId(), kbId));
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<AiChatSession> getSession(@PathVariable String sessionId) {
        return ApiResponse.success(sessionService.getSession(sessionId));
    }

    @Operation(summary = "获取会话消息历史")
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<AiChatMessage>> getMessages(@PathVariable String sessionId) {
        return ApiResponse.success(sessionService.getMessages(sessionId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId) {
        sessionService.deleteSession(sessionId);
        return ApiResponse.success();
    }

    @Operation(summary = "发送消息（带历史上下文）")
    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<Map<String, Object>> sendMessage(
            @PathVariable String sessionId,
            @RequestParam String question,
            @AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(sessionService.sendMessage(sessionId, question, user));
    }

    @Operation(summary = "智能问答（简单模式，无历史）")
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(
            @RequestParam String question,
            @RequestParam(required = false) Long kbId,
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal UserPrincipal user) {

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        Map<String, Object> result = chatService.chat(kbId, sessionId, question, user);
        return ApiResponse.success(result);
    }
}
