package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.entity.NavIntent;

import java.util.Map;

/**
 * 智能导航服务接口
 */
public interface NavIntentService extends IService<NavIntent> {

    /**
     * 根据用户输入识别意图
     */
    Map<String, Object> recognizeIntent(String query, Long userId);

    /**
     * 获取所有启用的意图
     */
    java.util.List<NavIntent> getActiveIntents();
}
