package com.gongan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     */
    PageResult<SysUser> pageList(Map<String, Object> params);

    /**
     * 根据用户名查询
     */
    SysUser getByUsername(String username);

    /**
     * 获取用户角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);

    /**
     * 获取用户权限标识列表
     */
    List<String> getPermissionsByUserId(Long userId);

    /**
     * 获取用户角色编码列表（别名方法）
     */
    default List<String> getUserRoleCodes(Long userId) {
        return getRoleCodesByUserId(userId);
    }

    /**
     * 获取用户权限标识列表（别名方法）
     */
    default List<String> getUserPermissions(Long userId) {
        return getPermissionsByUserId(userId);
    }

    /**
     * 获取用户菜单列表
     */
    List<Map<String, Object>> getUserMenus(Long userId);

    /**
     * 分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);
}
