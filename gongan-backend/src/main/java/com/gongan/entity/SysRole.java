package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 所属机构ID
     */
    private Long orgId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
