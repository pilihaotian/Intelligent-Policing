package com.gongan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.entity.SysMenu;
import com.gongan.mapper.SysMenuMapper;
import com.gongan.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<SysMenu> getMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDeleted, 0)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder);
        List<SysMenu> allMenus = list(wrapper);
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        Map<Long, List<SysMenu>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> rootMenus = menuMap.getOrDefault(parentId, new ArrayList<>());
        buildChildren(rootMenus, menuMap);
        return rootMenus;
    }

    private void buildChildren(List<SysMenu> menus, Map<Long, List<SysMenu>> menuMap) {
        for (SysMenu menu : menus) {
            List<SysMenu> children = menuMap.getOrDefault(menu.getId(), new ArrayList<>());
            buildChildren(children, menuMap);
            // 这里可以添加children字段，需要扩展SysMenu实体
        }
    }
}
