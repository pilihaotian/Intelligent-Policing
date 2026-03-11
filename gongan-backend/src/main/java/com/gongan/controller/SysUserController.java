package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysUser;
import com.gongan.filter.UserPrincipal;
import com.gongan.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/list")
    public ApiResponse<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long deptId) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (username != null) params.put("username", username);
        if (realName != null) params.put("realName", realName);
        if (status != null) params.put("status", status);
        if (orgId != null) params.put("orgId", orgId);
        if (deptId != null) params.put("deptId", deptId);

        return ApiResponse.success(userService.pageList(params));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public ApiResponse<SysUser> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(userService.getById(principal.getUserId()));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody SysUser user) {
        userService.save(user);
        return ApiResponse.success();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public ApiResponse<Void> update(@RequestBody SysUser user) {
        userService.updateById(user);
        return ApiResponse.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResponse.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ApiResponse.success();
    }
}
