package com.gongan.controller;

import com.gongan.dto.ApiResponse;
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
 */
@Tag(name = "智能导航")
@RestController
@RequestMapping("/nav")
@RequiredArgsConstructor
public class NavIntentController {

    private final NavIntentService navIntentService;

    @Operation(summary = "意图识别")
    @PostMapping("/recognize")
    public ApiResponse<Map<String, Object>> recognizeIntent(
            @RequestParam String query,
            @AuthenticationPrincipal UserPrincipal user) {
        Map<String, Object> result = navIntentService.recognizeIntent(query, user.getUserId());
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取所有意图列表")
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
}
