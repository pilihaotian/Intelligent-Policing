package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.entity.NavHistory;
import com.gongan.entity.NavIntent;

import java.util.List;
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
    List<NavIntent> getActiveIntents();

    /**
     * 保存导航历史
     */
    void saveNavHistory(Long userId, String inputText, String intentCode, String targetPath, boolean success, String message);

    /**
     * 获取当前用户导航历史（最近 N 条）
     */
    List<NavHistory> getNavHistory(Long userId, int limit);
}
