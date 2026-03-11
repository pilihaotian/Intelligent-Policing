package com.gongan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysRole;
import com.gongan.mapper.SysRoleMapper;
import com.gongan.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public PageResult<SysRole> pageList(Map<String, Object> params) {
        int current = params.containsKey("current") ? (int) params.get("current") : 1;
        int size = params.containsKey("size") ? (int) params.get("size") : 10;

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);

        if (params.containsKey("roleName")) {
            wrapper.like(SysRole::getRoleName, params.get("roleName"));
        }
        if (params.containsKey("roleCode")) {
            wrapper.like(SysRole::getRoleCode, params.get("roleCode"));
        }
        if (params.containsKey("status")) {
            wrapper.eq(SysRole::getStatus, params.get("status"));
        }
        if (params.containsKey("orgId")) {
            wrapper.eq(SysRole::getOrgId, params.get("orgId"));
        }

        wrapper.orderByAsc(SysRole::getSortOrder);
        wrapper.orderByDesc(SysRole::getCreatedTime);

        IPage<SysRole> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysRole> listAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有权限
        baseMapper.deleteRoleMenus(roleId);
        // 新增权限
        if (menuIds != null && !menuIds.isEmpty()) {
            baseMapper.insertRoleMenus(roleId, menuIds);
        }
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }
}
