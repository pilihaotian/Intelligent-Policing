package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysRole;

import java.util.List;
import java.util.Map;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色
     */
    PageResult<SysRole> pageList(Map<String, Object> params);

    /**
     * 获取所有启用状态的角色列表
     */
    List<SysRole> listAll();

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
