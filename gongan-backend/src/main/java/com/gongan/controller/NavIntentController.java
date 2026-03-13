package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.entity.NavHistory;
import com.gongan.entity.NavIntent;
import com.gongan.filter.UserPrincipal;
import com.gongan.service.NavIntentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能导航控制器
 * 
 * 提供以下功能：
 * 1. 意图识别 - 返回最匹配的导航目标
 * 2. 搜索建议 - 实时输入建议
 * 3. 智能推荐 - 基于历史和热门
 * 4. 快捷指令 - 简化输入
 * 5. 导航历史 - 记录查询历史
 */
@Tag(name = "智能导航")
@RestController
@RequestMapping("/nav")
@RequiredArgsConstructor
public class NavIntentController {

    private final NavIntentService navIntentService;

    @Operation(summary = "意图识别", description = "根据用户输入识别意图，返回相似度最高的前3个候选结果")
    @PostMapping("/recognize")
    public ApiResponse<Map<String, Object>> recognizeIntent(
            @RequestParam String query,
            @AuthenticationPrincipal UserPrincipal user) {
        Map<String, Object> result = navIntentService.recognizeIntent(query, user.getUserId());
        return ApiResponse.success(result);
    }

    @Operation(summary = "搜索建议", description = "用户输入时实时返回匹配建议")
    @GetMapping("/suggestions")
    public ApiResponse<List<Map<String, Object>>> getSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> suggestions = navIntentService.getSuggestions(query, limit);
        return ApiResponse.success(suggestions);
    }

    @Operation(summary = "智能推荐", description = "基于用户历史和热门功能返回推荐")
    @GetMapping("/recommendations")
    public ApiResponse<List<Map<String, Object>>> getRecommendations(
            @RequestParam(defaultValue = "6") int limit,
            @AuthenticationPrincipal UserPrincipal user) {
        List<Map<String, Object>> recommendations = navIntentService.getRecommendations(user.getUserId(), limit);
        return ApiResponse.success(recommendations);
    }

    @Operation(summary = "快捷指令列表", description = "获取所有可用的快捷指令")
    @GetMapping("/shortcuts")
    public ApiResponse<List<Map<String, Object>>> getShortcuts() {
        return ApiResponse.success(navIntentService.getShortcuts());
    }

    @Operation(summary = "热门导航", description = "获取全站热门导航统计")
    @GetMapping("/hot")
    public ApiResponse<List<Map<String, Object>>> getHotNavigations(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(navIntentService.getHotNavigations(limit));
    }

    @Operation(summary = "导航历史", description = "获取当前用户的导航历史")
    @GetMapping("/history")
    public ApiResponse<List<NavHistory>> getHistory(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal user) {
        List<NavHistory> list = navIntentService.getNavHistory(user.getUserId(), limit);
        return ApiResponse.success(list);
    }

    @Operation(summary = "获取所有意图列表", description = "获取系统中所有启用的导航意图")
    @GetMapping("/intent/list")
    public ApiResponse<List<NavIntent>> listIntents() {
        return ApiResponse.success(navIntentService.getActiveIntents());
    }

    @Operation(summary = "添加意图")
    @PostMapping("/intent")
    public ApiResponse<Void> addIntent(@RequestBody NavIntent intent) {
        navIntentService.save(intent);
        return ApiResponse.success();
    }

    @Operation(summary = "更新意图")
    @PutMapping("/intent")
    public ApiResponse<Void> updateIntent(@RequestBody NavIntent intent) {
        navIntentService.updateById(intent);
        return ApiResponse.success();
    }

    @Operation(summary = "删除意图")
    @DeleteMapping("/intent/{id}")
    public ApiResponse<Void> deleteIntent(@PathVariable Long id) {
        navIntentService.removeById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "同步菜单到意图表", description = "调用LLM分析菜单数据，生成导航关键词并写入nav_intent表")
    @PostMapping("/sync")
    public ApiResponse<Map<String, Object>> syncMenusToIntents() {
        Map<String, Object> result = navIntentService.syncMenusToIntents();
        return ApiResponse.success(result);
    }
}