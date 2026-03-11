package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.entity.SysMenu;
import com.gongan.filter.UserPrincipal;
import com.gongan.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/user")
    public ApiResponse<List<SysMenu>> getUserMenus(@AuthenticationPrincipal UserPrincipal principal) {
        List<SysMenu> menus = menuService.getUserMenus(principal.getUserId());
        return ApiResponse.success(menus);
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public ApiResponse<List<SysMenu>> getMenuTree() {
        return ApiResponse.success(menuService.getMenuTree());
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public ApiResponse<SysMenu> getById(@PathVariable Long id) {
        return ApiResponse.success(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return ApiResponse.success();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public ApiResponse<Void> update(@RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return ApiResponse.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/role/{roleId}")
    public ApiResponse<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        return ApiResponse.success(menuService.getMenuIdsByRoleId(roleId));
    }
}
