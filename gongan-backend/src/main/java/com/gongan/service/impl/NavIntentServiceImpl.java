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
 * 识别策略：① 关键词规则匹配（快速兜底）→ ② LLM意图推理（复杂/歧义场景）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavIntentServiceImpl extends ServiceImpl<NavIntentMapper, NavIntent> implements NavIntentService {

    private final LLMClient llmClient;
    private final NavHistoryMapper navHistoryMapper;
    private final FraudAnalysisService fraudAnalysisService;

    /** 规则匹配时跳过的通用动词词组（非主题词，避免误匹配） */
    private static final Set<String> ACTION_STOPWORDS = Set.of(
            "查一下", "看看", "打开", "查看", "进入", "我要", "我想",
            "需要", "想要", "帮我", "帮忙", "可以", "怎么", "如何"
    );

    @Override
    public Map<String, Object> recognizeIntent(String query, Long userId) {
        List<NavIntent> intents = getActiveIntents();
        Map<String, Object> result = new HashMap<>();

        // ── Step 1：关键词规则匹配（快速、稳定，优先命中） ──────────────────
        NavIntent ruleMatched = matchByKeyword(query, intents);
        if (ruleMatched != null) {
            buildSuccessResult(result, ruleMatched, null, intents);
            saveNavHistory(userId, query, ruleMatched.getIntentCode(),
                    (String) result.get("targetPath"), true, null);
            log.info("规则命中[{}] query={}", ruleMatched.getIntentCode(), query);
            return result;
        }

        // ── Step 2：LLM意图推理（处理口语、实体名、复杂说法） ──────────────
        String systemPrompt = buildSystemPrompt(intents);

        try {
            String aiResponse = llmClient.chatJson(
                    List.of(Map.of("role", "user", "content", "用户输入：" + query)),
                    systemPrompt
            );

            String jsonStr = extractJson(aiResponse);
            JSONObject parsed = JSON.parseObject(jsonStr);
            Integer n = parsed.getInteger("n");
            String entityKw = parsed.getString("e");
            if (entityKw != null) entityKw = entityKw.trim();
            if (entityKw != null && entityKw.isEmpty()) entityKw = null;

            if (n != null && n >= 0 && n < intents.size()) {
                NavIntent matched = intents.get(n);
                buildSuccessResult(result, matched, entityKw, intents);
                result.put("confidence", parsed.getBigDecimal("c"));
                saveNavHistory(userId, query, matched.getIntentCode(),
                        (String) result.get("targetPath"), true, (String) result.get("message"));
                log.info("LLM命中[{}] confidence={} query={}", matched.getIntentCode(), parsed.get("c"), query);
            } else {
                result.put("matched", false);
                result.put("success", false);
                result.put("message", "无法识别您的意图，请尝试更具体的描述");
                saveNavHistory(userId, query, null, null, false, (String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("LLM意图识别失败 query={}", query, e);
            result.put("matched", false);
            result.put("success", false);
            result.put("message", "意图识别失败，请稍后重试");
            saveNavHistory(userId, query, null, null, false, (String) result.get("message"));
        }

        return result;
    }

    // ── 关键词规则匹配：平方加权，长词优先，排除通用动词 ──────────────────
    private NavIntent matchByKeyword(String query, List<NavIntent> intents) {
        String q = query.toLowerCase().replaceAll("\\s+", "");
        NavIntent best = null;
        int bestScore = 0;

        for (NavIntent intent : intents) {
            if (intent.getKeywords() == null) continue;
            int score = 0;
            for (String kw : intent.getKeywords().split("[,，]")) {
                String k = kw.trim().toLowerCase();
                // 跳过1字词、通用动词
                if (k.length() < 2 || ACTION_STOPWORDS.contains(k)) continue;
                if (q.contains(k)) {
                    score += k.length() * k.length(); // 平方加权：4字词得16分，远高于2字词4分
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = intent;
            }
        }
        // 至少命中一个2字以上主题关键词（score >= 4）才采用规则匹配
        return bestScore >= 4 ? best : null;
    }

    // ── 构建紧凑高效的LLM提示词（含few-shot示例） ────────────────────────
    private String buildSystemPrompt(List<NavIntent> intents) {
        StringBuilder intentList = new StringBuilder();
        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            intentList.append(i).append(". ")
                    .append(intent.getIntentName())
                    .append(" [").append(intent.getKeywords()).append("]\n");
        }

        // 动态生成few-shot（基于真实意图序号，防止序号变化时示例错乱）
        String fewShot = buildFewShot(intents);

        return "你是智慧公安系统的智能导航助手。\n"
                + "任务：从下方编号列表选出最匹配用户输入的意图编号，只输出JSON，不要任何解释。\n\n"
                + "意图列表（编号. 名称 [关键词]）：\n" + intentList
                + "\n输出格式（仅一个JSON对象，不要其他内容）：\n"
                + "{\"n\":编号整数或null,\"c\":置信度0-100,\"e\":用户提到的人名或null}\n\n"
                + "规则：\n"
                + "- n 是意图编号（从0开始），完全无法判断时为null\n"
                + "- e 若用户明确提到某人姓名则填入，否则为null\n\n"
                + fewShot;
    }

    private String buildFewShot(List<NavIntent> intents) {
        StringBuilder sb = new StringBuilder("输入→输出示例：\n");
        int suspicious = getIntentIndex(intents, "SUSPICIOUS_PERSON");
        int analysis   = getIntentIndex(intents, "CASE_ANALYSIS");
        int transaction = getIntentIndex(intents, "TRANSACTION_LIST");
        int chat       = getIntentIndex(intents, "AI_CHAT");
        int role       = getIntentIndex(intents, "ROLE_MANAGE");
        int clue       = getIntentIndex(intents, "CLUE_MANAGE");

        sb.append("\"查重点人员\" → {\"n\":").append(suspicious).append(",\"c\":98,\"e\":null}\n");
        sb.append("\"查张三\" → {\"n\":").append(suspicious).append(",\"c\":85,\"e\":\"张三\"}\n");
        sb.append("\"看李四的案情分析\" → {\"n\":").append(analysis).append(",\"c\":90,\"e\":\"李四\"}\n");
        sb.append("\"张三的资金流水\" → {\"n\":").append(transaction).append(",\"c\":88,\"e\":\"张三\"}\n");
        sb.append("\"打开智能问答\" → {\"n\":").append(chat).append(",\"c\":99,\"e\":null}\n");
        sb.append("\"角色管理\" → {\"n\":").append(role).append(",\"c\":99,\"e\":null}\n");
        sb.append("\"查线索\" → {\"n\":").append(clue).append(",\"c\":95,\"e\":null}\n");
        sb.append("\"乱七八糟的输入\" → {\"n\":null,\"c\":0,\"e\":null}\n");
        return sb.toString();
    }

    // ── 构建成功返回结果（含实体路径增强） ────────────────────────────────
    private void buildSuccessResult(Map<String, Object> result, NavIntent matched,
            String entityKeyword, List<NavIntent> intents) {
        String targetPath = matched.getTargetPath();
        if (entityKeyword != null && !entityKeyword.isBlank()) {
            targetPath = enhancePathWithEntity(targetPath, entityKeyword, result);
        }
        result.put("matched", true);
        result.put("success", true);
        result.put("intentId", matched.getId());
        result.put("intentName", matched.getIntentName());
        result.put("targetName", matched.getIntentName());
        result.put("targetPath", targetPath);

        // 推荐其它意图（最多3个）
        List<Map<String, Object>> recs = new ArrayList<>();
        for (NavIntent other : intents) {
            if (other.getId().equals(matched.getId())) continue;
            Map<String, Object> rec = new HashMap<>();
            rec.put("intentCode", other.getIntentCode());
            rec.put("targetPath", other.getTargetPath());
            rec.put("intentName", other.getIntentName());
            rec.put("description", other.getDescription());
            recs.add(rec);
            if (recs.size() >= 3) break;
        }
        result.put("recommendations", recs);
    }

    // ── 实体增强：按姓名查人员，拼接带参路径 ─────────────────────────────
    private String enhancePathWithEntity(String targetPath, String entityKeyword,
            Map<String, Object> result) {
        try {
            PageResult<FraudSuspiciousCustomer> page = fraudAnalysisService.pageCustomers(
                    Map.of("current", 1, "size", 5, "customerName", entityKeyword));
            List<FraudSuspiciousCustomer> records = page.getRecords();
            if (records != null && !records.isEmpty()) {
                Long personId = records.get(0).getId();
                if ("/anti-fraud/analysis".equals(targetPath) && records.size() == 1) {
                    return targetPath + "?customerId=" + personId;
                } else if ("/anti-fraud/transaction".equals(targetPath)) {
                    return targetPath + "?customerId=" + personId;
                } else {
                    String enc = URLEncoder.encode(entityKeyword, StandardCharsets.UTF_8);
                    return targetPath + (targetPath.contains("?") ? "&" : "?") + "customerName=" + enc;
                }
            } else {
                result.put("message", "未找到人员「" + entityKeyword + "」，已打开对应列表页");
            }
        } catch (Exception e) {
            log.warn("人员查询失败 entityKeyword={}", entityKeyword, e);
        }
        return targetPath;
    }

    // ── 获取意图在列表中的序号 ──────────────────────────────────────────
    private int getIntentIndex(List<NavIntent> intents, String intentCode) {
        for (int i = 0; i < intents.size(); i++) {
            if (intentCode.equals(intents.get(i).getIntentCode())) return i;
        }
        return 0;
    }

    // ── 从LLM响应中提取JSON字符串（兼容markdown代码块、思考前缀等） ──────
    private String extractJson(String text) {
        if (text == null || text.isBlank()) return "{}";
        text = text.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
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
    public void saveNavHistory(Long userId, String inputText, String intentCode, String targetPath,
            boolean success, String message) {
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
