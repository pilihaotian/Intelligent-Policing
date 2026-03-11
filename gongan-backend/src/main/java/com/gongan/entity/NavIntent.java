package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导航意图实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nav_intent")
public class NavIntent extends BaseEntity {

    /**
     * 意图编码
     */
    private String intentCode;

    /**
     * 意图名称
     */
    private String intentName;

    /**
     * 意图描述
     */
    private String description;

    /**
     * 关键词数组
     */
    private String keywords;

    /**
     * 目标路径
     */
    private String targetPath;

    /**
     * 关联菜单ID
     */
    private Long menuId;

    /**
     * 示例提示词
     */
    private String examplePrompts;

    /**
     * 状态
     */
    private Integer status;
}
