package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取用户的菜单树
     */
    List<SysMenu> getUserMenus(Long userId);

    /**
     * 获取所有菜单树
     */
    List<SysMenu> getMenuTree();

    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
