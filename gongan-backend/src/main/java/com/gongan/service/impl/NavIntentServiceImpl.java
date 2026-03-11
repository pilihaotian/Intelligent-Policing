package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.entity.NavIntent;
import com.gongan.mapper.NavIntentMapper;
import com.gongan.service.NavIntentService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能导航服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavIntentServiceImpl extends ServiceImpl<NavIntentMapper, NavIntent> implements NavIntentService {

    private final LLMClient llmClient;

    @Override
    public Map<String, Object> recognizeIntent(String query, Long userId) {
        // 获取所有启用的意图
        List<NavIntent> intents = getActiveIntents();

        // 构建意图列表描述
        StringBuilder intentDesc = new StringBuilder();
        intentDesc.append("以下是系统中可用的导航目标：\n\n");

        Map<String, NavIntent> intentMap = new HashMap<>();
        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            String key = "intent_" + i;
            intentMap.put(key, intent);

            intentDesc.append(String.format("%d. %s (%s)\n", i + 1, intent.getIntentName(), intent.getDescription()));
            intentDesc.append(String.format("   关键词：%s\n", intent.getKeywords()));
            intentDesc.append(String.format("   目标路径：%s\n\n", intent.getTargetPath()));
        }

        // 构建AI提示词
        String systemPrompt = """
                你是一个智能导航助手，帮助用户快速找到他们想要的功能页面。
                
                根据用户的输入，判断用户的意图，并返回最匹配的导航目标。
                
                请以JSON格式返回结果：
                {
                    "intent_key": "匹配的意图key（如intent_0）或null",
                    "confidence": 置信度(0-100),
                    "reason": "判断理由"
                }
                
                如果无法确定用户意图，intent_key返回null。
                """;

        String userPrompt = "用户输入：" + query + "\n\n可用意图：\n" + intentDesc;

        // 调用LLM
        String aiResponse = llmClient.chat(List.of(
                Map.of("role", "user", "content", userPrompt)
        ), systemPrompt);

        // 解析结果
        Map<String, Object> result = new HashMap<>();
        try {
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```json") + 7, aiResponse.lastIndexOf("```"));
            } else if (aiResponse.contains("```")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```") + 3, aiResponse.lastIndexOf("```"));
            }

            JSONObject parsed = JSON.parseObject(jsonStr.trim());
            String intentKey = parsed.getString("intent_key");

            if (intentKey != null && intentMap.containsKey(intentKey)) {
                NavIntent matchedIntent = intentMap.get(intentKey);
                result.put("matched", true);
                result.put("intentId", matchedIntent.getId());
                result.put("intentName", matchedIntent.getIntentName());
                result.put("targetPath", matchedIntent.getTargetPath());
                result.put("confidence", parsed.getBigDecimal("confidence"));
                result.put("reason", parsed.getString("reason"));
            } else {
                result.put("matched", false);
                result.put("message", "无法识别您的意图，请尝试更具体的描述");
            }
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            result.put("matched", false);
            result.put("message", "意图识别失败，请稍后重试");
        }

        // 记录导航历史（实际项目中应保存到数据库）
        log.info("用户 {} 导航查询: {}, 结果: {}", userId, query, result);

        return result;
    }

    @Override
    public List<NavIntent> getActiveIntents() {
        LambdaQueryWrapper<NavIntent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NavIntent::getDeleted, 0)
                .eq(NavIntent::getStatus, 1)
                .orderByAsc(NavIntent::getId);
        return list(wrapper);
    }
}
