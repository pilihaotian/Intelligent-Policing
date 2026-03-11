package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.LoginRequest;
import com.gongan.dto.LoginResponse;
import com.gongan.entity.SysUser;
import com.gongan.filter.UserPrincipal;
import com.gongan.service.AuthService;
import com.gongan.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public ApiResponse<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserPrincipal user) {
        SysUser sysUser = userService.getById(user.getUserId());
        List<String> roles = userService.getUserRoleCodes(user.getUserId());
        List<String> permissions = userService.getUserPermissions(user.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", sysUser.getId());
        result.put("username", sysUser.getUsername());
        result.put("realName", sysUser.getRealName());
        result.put("email", sysUser.getEmail());
        result.put("phone", sysUser.getPhone());
        result.put("avatar", sysUser.getAvatar());
        result.put("orgId", sysUser.getOrgId());
        result.put("deptId", sysUser.getDeptId());
        result.put("roles", roles);
        result.put("permissions", permissions);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取用户菜单")
    @GetMapping("/menus")
    public ApiResponse<List<Map<String, Object>>> getMenus(@AuthenticationPrincipal UserPrincipal user) {
        List<Map<String, Object>> menus = userService.getUserMenus(user.getUserId());
        return ApiResponse.success(menus);
    }
}
