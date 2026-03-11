package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysRole;
import com.gongan.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "分页查询角色")
    @GetMapping("/list")
    public ApiResponse<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long orgId) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (roleName != null) params.put("roleName", roleName);
        if (roleCode != null) params.put("roleCode", roleCode);
        if (status != null) params.put("status", status);
        if (orgId != null) params.put("orgId", orgId);

        return ApiResponse.success(roleService.pageList(params));
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("/all")
    public ApiResponse<List<SysRole>> listAll() {
        return ApiResponse.success(roleService.listAll());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public ApiResponse<SysRole> getById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        return ApiResponse.success();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public ApiResponse<Void> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return ApiResponse.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "分配菜单权限")
    @PostMapping("/{id}/menus")
    public ApiResponse<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ApiResponse.success();
    }

    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/{id}/menus")
    public ApiResponse<List<Long>> getMenuIds(@PathVariable Long id) {
        return ApiResponse.success(roleService.getMenuIdsByRoleId(id));
    }
}
