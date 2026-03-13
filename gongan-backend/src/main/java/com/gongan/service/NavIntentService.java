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
     * 智能意图识别 - 返回相似度最高的前3个候选
     * @param query 用户输入
     * @param userId 用户ID
     * @return 识别结果（含candidates列表）
     */
    Map<String, Object> recognizeIntent(String query, Long userId);

    /**
     * 实时搜索建议 - 用户输入时返回匹配的意图列表
     * @param query 用户输入（部分）
     * @param limit 返回数量限制
     * @return 建议列表
     */
    List<Map<String, Object>> getSuggestions(String query, int limit);

    /**
     * 获取智能推荐 - 基于用户历史和热门功能
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 推荐列表
     */
    List<Map<String, Object>> getRecommendations(Long userId, int limit);

    /**
     * 获取所有启用的意图
     */
    List<NavIntent> getActiveIntents();

    /**
     * 获取快捷指令列表
     */
    List<Map<String, Object>> getShortcuts();

    /**
     * 保存导航历史
     */
    void saveNavHistory(Long userId, String inputText, String intentCode, String targetPath, boolean success, String message);

    /**
     * 获取当前用户导航历史
     */
    List<NavHistory> getNavHistory(Long userId, int limit);

    /**
     * 获取热门导航（全站统计）
     */
    List<Map<String, Object>> getHotNavigations(int limit);

    /**
     * 同步菜单到意图表 - 调用LLM分析菜单生成意图关键词
     * @return 同步结果
     */
    Map<String, Object> syncMenusToIntents();
}