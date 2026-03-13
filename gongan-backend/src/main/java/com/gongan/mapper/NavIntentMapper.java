package com.gongan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongan.entity.NavIntent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NavIntentMapper extends BaseMapper<NavIntent> {
    
    /**
     * 物理删除所有记录（绕过 @TableLogic 软删除）
     */
    @Delete("DELETE FROM nav_intent")
    int physicalDeleteAll();
}
