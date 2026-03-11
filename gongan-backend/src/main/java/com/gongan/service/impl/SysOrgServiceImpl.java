package com.gongan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysOrg;
import com.gongan.mapper.SysOrgMapper;
import com.gongan.service.SysOrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 机构服务实现
 */
@Service
@RequiredArgsConstructor
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @Override
    public PageResult<SysOrg> pageList(Map<String, Object> params) {
        int current = params.containsKey("current") ? (int) params.get("current") : 1;
        int size = params.containsKey("size") ? (int) params.get("size") : 10;

        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getDeleted, 0);

        if (params.containsKey("orgName")) {
            wrapper.like(SysOrg::getOrgName, params.get("orgName"));
        }
        if (params.containsKey("orgCode")) {
            wrapper.like(SysOrg::getOrgCode, params.get("orgCode"));
        }
        if (params.containsKey("status")) {
            wrapper.eq(SysOrg::getStatus, params.get("status"));
        }
        if (params.containsKey("parentId")) {
            wrapper.eq(SysOrg::getParentId, params.get("parentId"));
        }

        wrapper.orderByAsc(SysOrg::getSortOrder);
        wrapper.orderByDesc(SysOrg::getCreatedTime);

        IPage<SysOrg> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<Map<String, Object>> getOrgTree() {
        // 查询所有机构
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getDeleted, 0);
        wrapper.orderByAsc(SysOrg::getSortOrder);
        List<SysOrg> allOrgs = list(wrapper);

        // 构建树结构
        return buildTree(allOrgs, 0L);
    }

    @Override
    public List<SysOrg> getChildren(Long parentId) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getDeleted, 0);
        wrapper.eq(SysOrg::getParentId, parentId);
        wrapper.orderByAsc(SysOrg::getSortOrder);
        return list(wrapper);
    }

    /**
     * 递归构建树结构
     */
    private List<Map<String, Object>> buildTree(List<SysOrg> allOrgs, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 筛选出当前父节点的子节点
        List<SysOrg> children = allOrgs.stream()
                .filter(org -> Objects.equals(org.getParentId(), parentId))
                .collect(Collectors.toList());

        for (SysOrg org : children) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", org.getId());
            node.put("orgName", org.getOrgName());
            node.put("orgCode", org.getOrgCode());
            node.put("orgType", org.getOrgType());
            node.put("parentId", org.getParentId());
            node.put("level", org.getLevel());
            node.put("province", org.getProvince());
            node.put("city", org.getCity());
            node.put("address", org.getAddress());
            node.put("contactPhone", org.getContactPhone());
            node.put("status", org.getStatus());
            node.put("sortOrder", org.getSortOrder());

            // 递归获取子节点
            List<Map<String, Object>> childNodes = buildTree(allOrgs, org.getId());
            if (!childNodes.isEmpty()) {
                node.put("children", childNodes);
            }

            result.add(node);
        }

        return result;
    }
}
