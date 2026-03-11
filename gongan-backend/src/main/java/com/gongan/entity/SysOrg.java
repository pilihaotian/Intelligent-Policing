package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机构实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 机构类型
     */
    private String orgType;

    /**
     * 父机构ID
     */
    private Long parentId;

    /**
     * 机构层级
     */
    private Integer level;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
