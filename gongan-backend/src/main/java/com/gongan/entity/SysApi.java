package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_api")
public class SysApi extends BaseEntity {

    /**
     * API编码
     */
    private String apiCode;

    /**
     * API名称
     */
    private String apiName;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * HTTP方法
     */
    private String httpMethod;

    /**
     * 所属模块
     */
    private String module;

    /**
     * API描述
     */
    private String description;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 状态
     */
    private Integer status;
}
