package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
     * 关键词（逗号分隔）
     */
    private String keywords;

    /**
     * 同义词（逗号分隔）
     */
    @TableField(exist = false)
    private String synonyms;

    /**
     * 目标路径
     */
    private String targetPath;

    /**
     * 关联菜单ID
     */
    @TableField(exist = false)
    private Long menuId;

    /**
     * 示例提示词（数据库表中无此字段）
     */
    @TableField(exist = false)
    private String examplePrompts;

    /**
     * 图标
     */
    @TableField(exist = false)
    private String icon;

    /**
     * 分类（用于分组展示）
     */
    @TableField(exist = false)
    private String category;

    /**
     * 权重（用于排序）
     */
    private Integer priority;

    /**
     * 状态（1启用，0禁用）
     */
    private Integer status;
}