package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.entity.NavHistory;
import com.gongan.entity.NavIntent;
import com.gongan.mapper.NavHistoryMapper;
import com.gongan.mapper.NavIntentMapper;
import com.gongan.service.FraudAnalysisService;
import com.gongan.service.NavIntentService;
import com.gongan.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 智能导航服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavIntentServiceImpl extends ServiceImpl<NavIntentMapper, NavIntent> implements NavIntentService {

    private final LLMClient llmClient;
    private final NavHistoryMapper navHistoryMapper;
    private final FraudAnalysisService fraudAnalysisService;

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

        // 构建AI提示词（支持复杂意图、口语化与实体解析）
        String systemPrompt = """
                你是一个智慧公安系统的智能导航助手。用户可能用口语、简称、或一句话里包含多件事，你需要识别其最想去的那个页面（主意图），并解析出提及的具体实体（如人员姓名）。
                
                规则：
                1. 只返回一个主意图：用户说“先看重点人员再分析”或“我想看看张三的流水”时，选最明确的一个（如“看张三的流水”选资金流水+实体张三）。
                2. 容忍口语与同义：如“查一下”“看看”“打开”“进…页”“有没有”都表示要去某页；“流水”“交易”“转账记录”指向资金流水；“角色”“权限”“赋权”指向角色管理。
                3. 从句子中抽取具体人员姓名或编号，填到 entity_keyword；若未提及具体人则 entity_type 和 entity_keyword 为 null。
                
                请严格以JSON格式返回（不要多余文字）：
                {
                    "intent_key": "匹配的意图key（如intent_0）或null",
                    "confidence": 置信度(0-100),
                    "reason": "简短判断理由",
                    "entity_type": "若用户提到具体人员则填 person，否则不填或null",
                    "entity_keyword": "用户提及的人员姓名或简称，如张三、李四，未提及则为null"
                }
                
                示例理解：“我想看看有没有张三的流水” -> 主意图为资金流水(intent_对应流水)，entity_type=person, entity_keyword=张三；“打开角色管理” -> 角色管理；“查一下高危人员” -> 重点人员。
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
                String targetPath = matchedIntent.getTargetPath();
                String entityType = parsed.getString("entity_type");
                String entityKeyword = parsed.getString("entity_keyword");
                if (entityKeyword != null) entityKeyword = entityKeyword.trim();
                if (entityType != null && entityKeyword != null && !entityKeyword.isEmpty()
                        && "person".equalsIgnoreCase(entityType)) {
                    PageResult<FraudSuspiciousCustomer> page = fraudAnalysisService.pageCustomers(
                            Map.of("current", 1, "size", 5, "customerName", entityKeyword));
                    List<FraudSuspiciousCustomer> records = page.getRecords();
                    if (records != null && !records.isEmpty()) {
                        Long personId = records.get(0).getId();
                        if ("/anti-fraud/analysis".equals(targetPath) && records.size() == 1) {
                            targetPath = targetPath + "?customerId=" + personId;
                        } else if ("/anti-fraud/transaction".equals(targetPath)) {
                            targetPath = targetPath + "?customerId=" + personId;
                        } else {
                            String enc = URLEncoder.encode(entityKeyword, StandardCharsets.UTF_8);
                            if (targetPath.contains("?")) targetPath = targetPath + "&customerName=" + enc;
                            else targetPath = targetPath + "?customerName=" + enc;
                        }
                    } else {
                        result.put("message", "未找到匹配人员「" + entityKeyword + "」，已打开对应列表页");
                    }
                }
                result.put("matched", true);
                result.put("success", true);
                result.put("intentId", matchedIntent.getId());
                result.put("intentName", matchedIntent.getIntentName());
                result.put("targetName", matchedIntent.getIntentName());
                result.put("targetPath", targetPath);
                result.put("confidence", parsed.getBigDecimal("confidence"));
                result.put("reason", parsed.getString("reason"));
                // 推荐 2～3 个其它意图
                List<Map<String, Object>> recommendations = new ArrayList<>();
                for (NavIntent other : intents) {
                    if (other.getId().equals(matchedIntent.getId())) continue;
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("intentCode", other.getIntentCode());
                    rec.put("targetPath", other.getTargetPath());
                    rec.put("intentName", other.getIntentName());
                    rec.put("description", other.getDescription());
                    recommendations.add(rec);
                    if (recommendations.size() >= 3) break;
                }
                result.put("recommendations", recommendations);
            } else {
                result.put("matched", false);
                result.put("success", false);
                result.put("message", "无法识别您的意图，请尝试更具体的描述");
            }
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            result.put("matched", false);
            result.put("success", false);
            result.put("message", "意图识别失败，请稍后重试");
        }

        // 导航历史落库
        String intentCode = null;
        String targetPath = (String) result.get("targetPath");
        Boolean success = (Boolean) result.get("success");
        String message = (String) result.get("message");
        if (result.get("intentId") != null) {
            NavIntent matched = intentMap.values().stream()
                    .filter(i -> i.getId().equals(result.get("intentId")))
                    .findFirst().orElse(null);
            if (matched != null) intentCode = matched.getIntentCode();
        }
        saveNavHistory(userId, query, intentCode, targetPath, Boolean.TRUE.equals(success), message);

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

    @Override
    public void saveNavHistory(Long userId, String inputText, String intentCode, String targetPath, boolean success, String message) {
        NavHistory h = new NavHistory();
        h.setUserId(userId);
        h.setInputText(inputText != null && inputText.length() > 500 ? inputText.substring(0, 500) : inputText);
        h.setIntentCode(intentCode);
        h.setTargetPath(targetPath);
        h.setSuccess(success ? 1 : 0);
        h.setMessage(message != null && message.length() > 500 ? message.substring(0, 500) : message);
        h.setCreatedTime(LocalDateTime.now());
        navHistoryMapper.insert(h);
    }

    @Override
    public List<NavHistory> getNavHistory(Long userId, int limit) {
        LambdaQueryWrapper<NavHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NavHistory::getUserId, userId)
                .orderByDesc(NavHistory::getCreatedTime)
                .last("LIMIT " + Math.min(limit, 50));
        return navHistoryMapper.selectList(wrapper);
    }
}
