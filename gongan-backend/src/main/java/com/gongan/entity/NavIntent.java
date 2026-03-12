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
     * 关键词数组
     */
    private String keywords;

    /**
     * 目标路径
     */
    private String targetPath;

    /**
     * 关联菜单ID（表中无此列，不参与持久化）
     */
    @TableField(exist = false)
    private Long menuId;

    /**
     * 示例提示词（表中无此列时不参与持久化，可执行 ALTER TABLE nav_intent ADD COLUMN example_prompts TEXT 后去掉 @TableField(exist = false)）
     */
    @TableField(exist = false)
    private String examplePrompts;

    /**
     * 状态
     */
    private Integer status;
}
