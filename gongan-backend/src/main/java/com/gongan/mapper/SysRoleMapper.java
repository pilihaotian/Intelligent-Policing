package com.gongan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongan.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 删除角色菜单关联
     */
    void deleteRoleMenus(@Param("roleId") Long roleId);

    /**
     * 批量插入角色菜单关联
     */
    void insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 查询角色的菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
