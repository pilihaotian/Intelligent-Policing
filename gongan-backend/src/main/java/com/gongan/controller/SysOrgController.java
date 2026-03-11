package com.gongan.controller;

import com.gongan.dto.ApiResponse;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysOrg;
import com.gongan.service.SysOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机构控制器
 */
@Tag(name = "机构管理")
@RestController
@RequestMapping("/system/org")
@RequiredArgsConstructor
public class SysOrgController {

    private final SysOrgService orgService;

    @Operation(summary = "分页查询机构")
    @GetMapping("/list")
    public ApiResponse<PageResult<SysOrg>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orgName,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long parentId) {

        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        if (orgName != null) params.put("orgName", orgName);
        if (orgCode != null) params.put("orgCode", orgCode);
        if (status != null) params.put("status", status);
        if (parentId != null) params.put("parentId", parentId);

        return ApiResponse.success(orgService.pageList(params));
    }

    @Operation(summary = "获取机构树")
    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.success(orgService.getOrgTree());
    }

    @Operation(summary = "获取机构详情")
    @GetMapping("/{id}")
    public ApiResponse<SysOrg> getById(@PathVariable Long id) {
        return ApiResponse.success(orgService.getById(id));
    }

    @Operation(summary = "新增机构")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody SysOrg org) {
        orgService.save(org);
        return ApiResponse.success();
    }

    @Operation(summary = "修改机构")
    @PutMapping
    public ApiResponse<Void> update(@RequestBody SysOrg org) {
        orgService.updateById(org);
        return ApiResponse.success();
    }

    @Operation(summary = "删除机构")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orgService.removeById(id);
        return ApiResponse.success();
    }
}
