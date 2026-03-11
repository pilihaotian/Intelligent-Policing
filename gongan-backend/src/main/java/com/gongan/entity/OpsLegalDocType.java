package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 法律文书类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_legal_doc_type")
public class OpsLegalDocType extends BaseEntity {

    /**
     * 文书类型编码
     */
    private String typeCode;

    /**
     * 文书类型名称
     */
    private String typeName;

    /**
     * 类型描述
     */
    private String description;

    /**
     * 字段配置(JSON格式)
     */
    private String fieldConfig;

    /**
     * 状态
     */
    private Integer status;
}
