package com.gongan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航历史实体
 */
@Data
@TableName("nav_history")
public class NavHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String inputText;
    private String intentCode;
    private String targetPath;
    /**
     * 1=成功 0=失败
     */
    private Integer success;
    private String message;
    private LocalDateTime createdTime;
}
