package com.gongan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysMenu;
import com.gongan.entity.SysUser;
import com.gongan.exception.BusinessException;
import com.gongan.mapper.SysMenuMapper;
import com.gongan.mapper.SysUserMapper;
import com.gongan.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysMenuMapper menuMapper;

    @Override
    public PageResult<SysUser> pageList(Map<String, Object> params) {
        int current = params.containsKey("current") ? (int) params.get("current") : 1;
        int size = params.containsKey("size") ? (int) params.get("size") : 10;

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, 0);

        if (params.containsKey("username")) {
            wrapper.like(SysUser::getUsername, params.get("username"));
        }
        if (params.containsKey("realName")) {
            wrapper.like(SysUser::getRealName, params.get("realName"));
        }
        if (params.containsKey("status")) {
            wrapper.eq(SysUser::getStatus, params.get("status"));
        }
        if (params.containsKey("orgId")) {
            wrapper.eq(SysUser::getOrgId, params.get("orgId"));
        }
        if (params.containsKey("deptId")) {
            wrapper.eq(SysUser::getDeptId, params.get("deptId"));
        }

        wrapper.orderByDesc(SysUser::getCreatedTime);

        IPage<SysUser> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getUserMenus(Long userId) {
        // 获取用户菜单列表
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", menu.getId());
            map.put("parentId", menu.getParentId());
            map.put("menuName", menu.getMenuName());
            map.put("menuCode", menu.getMenuCode());
            map.put("path", menu.getPath());
            map.put("component", menu.getComponent());
            map.put("icon", menu.getIcon());
            map.put("permission", menu.getPermission());
            map.put("sortOrder", menu.getSortOrder());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除原有关联
        baseMapper.deleteUserRoles(userId);
        // 新增关联
        if (roleIds != null && !roleIds.isEmpty()) {
            baseMapper.insertUserRoles(userId, roleIds);
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw BusinessException.of("密码不能为空");
        }
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public void addUser(SysUser user) {
        if (user == null) {
            return;
        }
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        save(user);
    }
}
