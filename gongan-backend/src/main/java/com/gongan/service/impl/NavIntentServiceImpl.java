package com.gongan.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongan.dto.PageResult;
import com.gongan.entity.FraudSuspiciousCustomer;
import com.gongan.entity.NavHistory;
import com.gongan.entity.NavIntent;
import com.gongan.entity.SysMenu;
import com.gongan.mapper.NavHistoryMapper;
import com.gongan.mapper.NavIntentMapper;
import com.gongan.mapper.SysMenuMapper;
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
import java.util.stream.Collectors;

/**
 * 智能导航服务实现
 * 
 * 匹配策略（优先级从高到低）：
 * 1. 精确匹配 - 关键词完全包含
 * 2. 同义词匹配 - 同义词库匹配
 * 3. 模糊匹配 - 部分匹配 + 拼音首字母
 * 4. LLM推理 - 自然语言理解
 * 
 * 返回：相似度最高的前3个候选结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavIntentServiceImpl extends ServiceImpl<NavIntentMapper, NavIntent> implements NavIntentService {

    private final LLMClient llmClient;
    private final NavHistoryMapper navHistoryMapper;
    private final FraudAnalysisService fraudAnalysisService;
    private final SysMenuMapper sysMenuMapper;

    /** 最高返回候选数量 */
    private static final int MAX_CANDIDATES = 3;

    /** 通用停用词（匹配时忽略） */
    private static final Set<String> STOP_WORDS = Set.of(
            "请", "帮我", "帮忙", "我要", "我想", "需要", "想要",
            "一下", "看看", "打开", "进入", "跳转", "去", "到", "里",
            "的", "了", "吗", "呢", "吧", "啊", "呀", "哦"
    );

    /** 同义词映射 */
    private static final Map<String, String[]> SYNONYMS = Map.ofEntries(
            // 人员相关
            Map.entry("重点人员", new String[]{"嫌疑人", "高危人员", "管控对象", "关注对象", "黑名单"}),
            Map.entry("人员核查", new String[]{"背景调查", "尽职调查", "人员调查", "背景核查"}),
            // 案件相关
            Map.entry("案件分析", new String[]{"案情分析", "侦查分析", "研判分析", "案件研判"}),
            Map.entry("线索管理", new String[]{"线索", "情报", "举报", "线索上报"}),
            // 文书相关
            Map.entry("法律文书", new String[]{"文书", "判决书", "笔录", "法律文书填报"}),
            // 系统管理
            Map.entry("机构管理", new String[]{"部门管理", "组织架构", "单位管理", "组织机构"}),
            Map.entry("用户管理", new String[]{"账号管理", "人员管理", "用户列表"}),
            Map.entry("角色管理", new String[]{"权限管理", "权限配置", "角色配置"}),
            // AI功能
            Map.entry("智能问答", new String[]{"AI问答", "智能助手", "AI助手", "问答系统"}),
            Map.entry("知识库", new String[]{"文档库", "知识库管理", "文档管理"}),
            // 资金相关
            Map.entry("资金流水", new String[]{"交易流水", "转账记录", "流水", "交易记录"}),
            Map.entry("可疑交易", new String[]{"异常交易", "可疑资金", "洗钱线索"})
    );

    /** 快捷指令 */
    private static final Map<String, String> SHORTCUTS = Map.of(
            "zk", "重点人员",
            "aj", "案件分析",
            "ls", "资金流水",
            "xs", "线索管理",
            "ws", "法律文书",
            "wd", "智能问答",
            "jg", "机构管理",
            "yh", "用户管理",
            "js", "角色管理",
            "hc", "人员核查"
    );

    @Override
    public Map<String, Object> recognizeIntent(String query, Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<NavIntent> intents = getActiveIntents();

        if (intents == null || intents.isEmpty()) {
            return buildErrorResponse("系统暂无可用导航意图");
        }

        // 预处理用户输入
        String normalizedQuery = normalizeQuery(query);
        log.debug("原始输入: {}, 标准化后: {}", query, normalizedQuery);

        // ── Step 1: 检查快捷指令 ─────────────────────────────────────────
        if (SHORTCUTS.containsKey(normalizedQuery)) {
            String targetName = SHORTCUTS.get(normalizedQuery);
            NavIntent matched = intents.stream()
                    .filter(i -> targetName.equals(i.getIntentName()))
                    .findFirst().orElse(null);
            if (matched != null) {
                return buildSuccessResult(matched, intents, query, 100, "快捷指令匹配");
            }
        }

        // ── Step 2: 精确匹配（关键词完全包含） ───────────────────────────────
        List<MatchResult> matchResults = new ArrayList<>();
        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            int score = calculateExactMatchScore(normalizedQuery, intent);
            if (score > 0) {
                matchResults.add(new MatchResult(i, score, score, "精确匹配"));
            }
        }

        // ── Step 3: 同义词匹配 ───────────────────────────────────────────
        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            int synonymScore = calculateSynonymScore(normalizedQuery, intent);
            if (synonymScore > 0) {
                // 检查是否已存在
                int finalI = i;
                MatchResult existing = matchResults.stream()
                        .filter(m -> m.index == finalI)
                        .findFirst().orElse(null);
                if (existing != null) {
                    existing.score = Math.max(existing.score, synonymScore);
                    existing.confidence = existing.score;
                } else {
                    matchResults.add(new MatchResult(i, synonymScore, synonymScore, "同义词匹配"));
                }
            }
        }

        // ── Step 4: 模糊匹配（部分关键词 + 拼音首字母） ───────────────────────
        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            int fuzzyScore = calculateFuzzyScore(normalizedQuery, intent);
            if (fuzzyScore > 0) {
                int finalI = i;
                MatchResult existing = matchResults.stream()
                        .filter(m -> m.index == finalI)
                        .findFirst().orElse(null);
                if (existing != null) {
                    existing.score = Math.max(existing.score, fuzzyScore);
                    existing.confidence = existing.score;
                } else {
                    matchResults.add(new MatchResult(i, fuzzyScore, fuzzyScore, "模糊匹配"));
                }
            }
        }

        // 排序
        matchResults.sort((a, b) -> Integer.compare(b.score, a.score));

        // ── Step 5: 如果规则匹配置信度足够高，直接返回 ─────────────────────
        if (!matchResults.isEmpty() && matchResults.get(0).score >= 4) {
            List<Map<String, Object>> candidates = buildCandidates(matchResults, intents, query, result);
            result.put("matched", true);
            result.put("success", true);
            result.put("candidates", candidates);
            result.put("bestMatch", candidates.get(0));
            result.put("matchType", matchResults.get(0).matchType);

            // 兼容旧接口
            copyBestMatch(result, candidates.get(0));
            saveNavHistory(userId, query, (String) candidates.get(0).get("intentCode"),
                    (String) candidates.get(0).get("targetPath"), true, null);
            return result;
        }

        // ── Step 6: LLM意图推理 ──────────────────────────────────────────
        try {
            List<MatchResult> llmResults = callLLMForIntent(query, intents);
            if (!llmResults.isEmpty()) {
                // 合并结果
                List<MatchResult> mergedResults = mergeResults(matchResults, llmResults);
                List<Map<String, Object>> candidates = buildCandidates(mergedResults, intents, query, result);
                
                result.put("matched", true);
                result.put("success", true);
                result.put("candidates", candidates);
                result.put("bestMatch", candidates.get(0));
                result.put("matchType", "LLM推理");

                copyBestMatch(result, candidates.get(0));
                saveNavHistory(userId, query, (String) candidates.get(0).get("intentCode"),
                        (String) candidates.get(0).get("targetPath"), true, null);
                return result;
            }
        } catch (Exception e) {
            log.warn("LLM调用失败，降级到规则匹配: {}", e.getMessage());
        }

        // ── Step 7: 降级处理 - 返回最相似的建议 ─────────────────────────────
        if (!matchResults.isEmpty()) {
            List<Map<String, Object>> suggestions = buildCandidates(
                    matchResults.subList(0, Math.min(MAX_CANDIDATES, matchResults.size())),
                    intents, query, result);
            result.put("matched", false);
            result.put("success", false);
            result.put("candidates", suggestions);
            result.put("message", "未找到精确匹配，您是否想要：");
            saveNavHistory(userId, query, null, null, false, (String) result.get("message"));
        } else {
            result.put("matched", false);
            result.put("success", false);
            result.put("candidates", Collections.emptyList());
            result.put("message", "无法识别您的意图，请尝试更具体的描述，如'查重点人员'或'案件分析'");
            saveNavHistory(userId, query, null, null, false, (String) result.get("message"));
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getSuggestions(String query, int limit) {
        List<NavIntent> intents = getActiveIntents();
        if (intents == null || query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedQuery = normalizeQuery(query);
        List<MatchResult> results = new ArrayList<>();

        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            int score = 0;
            
            // 检查意图名称是否以查询开头
            if (intent.getIntentName().toLowerCase().contains(normalizedQuery)) {
                score += 20;
            }
            
            // 检查关键词
            if (intent.getKeywords() != null) {
                for (String kw : intent.getKeywords().split("[,，]")) {
                    String k = kw.trim().toLowerCase();
                    if (k.startsWith(normalizedQuery)) {
                        score += 15;
                    } else if (k.contains(normalizedQuery)) {
                        score += 10;
                    }
                }
            }

            // 拼音首字母匹配
            String pinyin = getPinyinInitials(intent.getIntentName());
            if (normalizedQuery.length() <= 3 && pinyin.startsWith(normalizedQuery)) {
                score += 12;
            }

            if (score > 0) {
                results.add(new MatchResult(i, score, score, "建议"));
            }
        }

        results.sort((a, b) -> Integer.compare(b.score, a.score));
        return buildCandidates(results.subList(0, Math.min(limit, results.size())), intents, "", null);
    }

    @Override
    public List<Map<String, Object>> getRecommendations(Long userId, int limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        List<NavIntent> intents = getActiveIntents();

        // 获取用户最近访问的意图
        List<NavHistory> recentHistory = getNavHistory(userId, limit * 2);
        Map<String, Integer> intentCount = new HashMap<>();
        
        for (NavHistory h : recentHistory) {
            if (h.getIntentCode() != null) {
                intentCount.merge(h.getIntentCode(), 1, Integer::sum);
            }
        }

        // 按访问频率排序
        List<NavIntent> sortedIntents = intents.stream()
                .sorted((a, b) -> {
                    int countA = intentCount.getOrDefault(a.getIntentCode(), 0);
                    int countB = intentCount.getOrDefault(b.getIntentCode(), 0);
                    return Integer.compare(countB, countA);
                })
                .limit(limit)
                .collect(Collectors.toList());

        for (NavIntent intent : sortedIntents) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("intentCode", intent.getIntentCode());
            rec.put("intentName", intent.getIntentName());
            rec.put("targetPath", intent.getTargetPath());
            rec.put("description", intent.getDescription());
            rec.put("frequency", intentCount.getOrDefault(intent.getIntentCode(), 0));
            recommendations.add(rec);
        }

        return recommendations;
    }

    @Override
    public List<Map<String, Object>> getShortcuts() {
        List<Map<String, Object>> shortcuts = new ArrayList<>();
        List<NavIntent> intents = getActiveIntents();

        for (Map.Entry<String, String> entry : SHORTCUTS.entrySet()) {
            String code = entry.getKey();
            String targetName = entry.getValue();
            
            NavIntent intent = intents.stream()
                    .filter(i -> targetName.equals(i.getIntentName()))
                    .findFirst().orElse(null);
            
            if (intent != null) {
                Map<String, Object> shortcut = new HashMap<>();
                shortcut.put("code", code);
                shortcut.put("name", intent.getIntentName());
                shortcut.put("path", intent.getTargetPath());
                shortcuts.add(shortcut);
            }
        }

        return shortcuts;
    }

    @Override
    public List<NavIntent> getActiveIntents() {
        LambdaQueryWrapper<NavIntent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NavIntent::getDeleted, 0)
                .eq(NavIntent::getStatus, 1)
                .orderByAsc(NavIntent::getPriority)
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

    @Override
    public List<Map<String, Object>> getHotNavigations(int limit) {
        List<Map<String, Object>> hotList = new ArrayList<>();
        
        // 统计最近7天的导航次数
        LambdaQueryWrapper<NavHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(NavHistory::getCreatedTime, LocalDateTime.now().minusDays(7))
                .eq(NavHistory::getSuccess, 1)
                .isNotNull(NavHistory::getIntentCode);
        
        List<NavHistory> histories = navHistoryMapper.selectList(wrapper);
        Map<String, Long> countMap = histories.stream()
                .collect(Collectors.groupingBy(NavHistory::getIntentCode, Collectors.counting()));

        // 排序并返回
        List<NavIntent> intents = getActiveIntents();
        intents.stream()
                .filter(i -> countMap.containsKey(i.getIntentCode()))
                .sorted((a, b) -> Long.compare(
                        countMap.getOrDefault(b.getIntentCode(), 0L),
                        countMap.getOrDefault(a.getIntentCode(), 0L)))
                .limit(limit)
                .forEach(intent -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("intentCode", intent.getIntentCode());
                    item.put("intentName", intent.getIntentName());
                    item.put("targetPath", intent.getTargetPath());
                    item.put("count", countMap.get(intent.getIntentCode()));
                    hotList.add(item);
                });

        return hotList;
    }

    // ==================== 私有方法 ====================

    private String normalizeQuery(String query) {
        if (query == null) return "";
        String normalized = query.toLowerCase().replaceAll("\\s+", "");
        // 移除停用词
        for (String stop : STOP_WORDS) {
            normalized = normalized.replace(stop, "");
        }
        return normalized;
    }

    private int calculateExactMatchScore(String query, NavIntent intent) {
        if (intent.getKeywords() == null) return 0;
        
        int maxScore = 0;
        for (String kw : intent.getKeywords().split("[,，]")) {
            String k = kw.trim().toLowerCase();
            if (k.length() < 2) continue;
            
            if (query.contains(k)) {
                // 平方加权：长词得分更高
                int score = k.length() * k.length();
                // 完全匹配加分
                if (query.equals(k)) {
                    score += 20;
                }
                maxScore = Math.max(maxScore, score);
            }
        }
        return maxScore;
    }

    private int calculateSynonymScore(String query, NavIntent intent) {
        if (intent.getKeywords() == null) return 0;
        
        int maxScore = 0;
        String[] keywords = intent.getKeywords().split("[,，]");
        
        for (String kw : keywords) {
            String k = kw.trim().toLowerCase();
            String[] synonyms = SYNONYMS.get(k);
            if (synonyms != null) {
                for (String syn : synonyms) {
                    if (query.contains(syn.toLowerCase())) {
                        // 同义词匹配得分略低于精确匹配
                        int score = syn.length() * syn.length() * 8 / 10;
                        maxScore = Math.max(maxScore, score);
                    }
                }
            }
        }
        return maxScore;
    }

    private int calculateFuzzyScore(String query, NavIntent intent) {
        if (intent.getKeywords() == null) return 0;
        
        int score = 0;
        String[] keywords = intent.getKeywords().split("[,，]");
        
        for (String kw : keywords) {
            String k = kw.trim().toLowerCase();
            if (k.length() < 2) continue;
            
            // 部分匹配
            int matchLen = 0;
            for (int i = 0; i < k.length() && i < query.length(); i++) {
                if (query.contains(k.substring(0, i + 1))) {
                    matchLen = i + 1;
                }
            }
            if (matchLen >= 2) {
                score += matchLen * matchLen / 2;
            }
        }

        // 拼音首字母匹配
        String pinyin = getPinyinInitials(intent.getIntentName());
        if (query.length() <= 4 && pinyin.contains(query)) {
            score += query.length() * 3;
        }

        return score;
    }

    private String getPinyinInitials(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            // 简化的拼音首字母提取
            String pinyin = getPinyinInitial(c);
            if (pinyin != null) {
                sb.append(pinyin);
            }
        }
        return sb.toString().toLowerCase();
    }

    private String getPinyinInitial(char c) {
        // 简化的拼音首字母映射
        int code = (int) c;
        if (code >= 'A' && code <= 'Z') return String.valueOf((char) (code + 32));
        if (code >= 'a' && code <= 'z') return String.valueOf(c);
        
        // 简化的汉字拼音首字母映射
        if (code >= 0x4E00 && code <= 0x9FA5) {
            // 常见汉字拼音首字母
            String[] pinyinMap = {
                    "重", "案", "线", "法", "智", "知", "资", "可", "机", "用", "角", "人"
            };
            String[] initials = {"z", "a", "x", "f", "z", "z", "z", "k", "j", "y", "j", "r"};
            for (int i = 0; i < pinyinMap.length; i++) {
                if (c == pinyinMap[i].charAt(0)) {
                    return initials[i];
                }
            }
            // 其他汉字取第一个可能的拼音
            return String.valueOf((char) ('a' + (code % 26)));
        }
        return null;
    }

    private List<MatchResult> callLLMForIntent(String query, List<NavIntent> intents) {
        String systemPrompt = buildLLMPrompt(intents);
        
        try {
            String response = llmClient.chatJson(
                    List.of(Map.of("role", "user", "content", "用户输入：" + query)),
                    systemPrompt
            );

            JSONObject parsed = JSON.parseObject(extractJson(response));
            List<MatchResult> results = new ArrayList<>();

            JSONArray candidates = parsed.getJSONArray("candidates");
            if (candidates != null) {
                for (int i = 0; i < candidates.size() && i < MAX_CANDIDATES; i++) {
                    JSONObject cand = candidates.getJSONObject(i);
                    Integer n = cand.getInteger("n");
                    Double c = cand.getDouble("c");
                    if (n != null && n >= 0 && n < intents.size() && c != null && c >= 40) {
                        results.add(new MatchResult(n, (int) (c * 1.0), c, "LLM推理"));
                    }
                }
            }

            return results;
        } catch (Exception e) {
            log.error("LLM调用异常", e);
            return Collections.emptyList();
        }
    }

    private String buildLLMPrompt(List<NavIntent> intents) {
        StringBuilder intentList = new StringBuilder();
        for (int i = 0; i < intents.size(); i++) {
            NavIntent intent = intents.get(i);
            intentList.append(i).append(". ")
                    .append(intent.getIntentName())
                    .append(" [关键词: ").append(intent.getKeywords()).append("]\n");
        }

        return "你是智慧公安系统的智能导航助手。\n\n"
                + "任务：根据用户输入，返回最匹配的导航意图（最多3个），按置信度降序排列。\n\n"
                + "可选意图列表：\n" + intentList + "\n"
                + "输出JSON格式：\n"
                + "{\"candidates\":[{\"n\":意图编号,\"c\":置信度0-100},{...}],\"e\":\"人名或null\"}\n\n"
                + "规则：\n"
                + "- 置信度 >= 60 才算有效匹配\n"
                + "- n 为意图编号(从0开始)\n"
                + "- 若用户提到人名，填入 e 字段";
    }

    private List<MatchResult> mergeResults(List<MatchResult> ruleResults, List<MatchResult> llmResults) {
        Map<Integer, MatchResult> merged = new LinkedHashMap<>();
        
        // LLM结果权重更高
        for (MatchResult r : llmResults) {
            merged.put(r.index, new MatchResult(r.index, r.score, r.confidence, r.matchType));
        }
        
        // 合并规则结果
        for (MatchResult r : ruleResults) {
            if (merged.containsKey(r.index)) {
                MatchResult existing = merged.get(r.index);
                // 综合：LLM权重0.6 + 规则权重0.4
                double newConfidence = existing.confidence * 0.6 + r.confidence * 0.4;
                merged.put(r.index, new MatchResult(r.index, (int) newConfidence, newConfidence, existing.matchType));
            } else if (merged.size() < MAX_CANDIDATES) {
                merged.put(r.index, r);
            }
        }

        List<MatchResult> result = new ArrayList<>(merged.values());
        result.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        return result.subList(0, Math.min(MAX_CANDIDATES, result.size()));
    }

    private List<Map<String, Object>> buildCandidates(List<MatchResult> results,
            List<NavIntent> intents, String query, Map<String, Object> context) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        String entityKeyword = extractEntity(query);

        for (MatchResult r : results) {
            if (r.index < 0 || r.index >= intents.size()) continue;
            
            NavIntent intent = intents.get(r.index);
            Map<String, Object> cand = new HashMap<>();
            
            String targetPath = intent.getTargetPath();
            if (candidates.isEmpty() && entityKeyword != null) {
                targetPath = enhancePathWithEntity(targetPath, entityKeyword, context);
            }

            cand.put("intentId", intent.getId());
            cand.put("intentCode", intent.getIntentCode());
            cand.put("intentName", intent.getIntentName());
            cand.put("targetPath", targetPath);
            cand.put("description", intent.getDescription());
            cand.put("confidence", Math.min(100, r.confidence));
            cand.put("matchType", r.matchType);
            candidates.add(cand);
        }

        return candidates;
    }

    private String extractEntity(String query) {
        if (query == null) return null;
        // 简单的人名提取
        for (String prefix : new String[]{"查", "看", "找", "关于"}) {
            int idx = query.indexOf(prefix);
            if (idx >= 0 && idx < query.length() - 2) {
                String rest = query.substring(idx + prefix.length()).trim();
                String[] parts = rest.split("[\\s,，。、！？的]");
                if (parts.length > 0 && parts[0].length() >= 2 && parts[0].length() <= 4) {
                    return parts[0];
                }
            }
        }
        return null;
    }

    private String enhancePathWithEntity(String path, String entity, Map<String, Object> context) {
        try {
            PageResult<FraudSuspiciousCustomer> page = fraudAnalysisService.pageCustomers(
                    Map.of("current", 1, "size", 1, "customerName", entity));
            if (page.getRecords() != null && !page.getRecords().isEmpty()) {
                Long personId = page.getRecords().get(0).getId();
                if (path.contains("analysis") || path.contains("transaction")) {
                    return path + "?customerId=" + personId;
                }
                return path + "?customerName=" + URLEncoder.encode(entity, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("实体增强失败: {}", e.getMessage());
        }
        return path;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("matched", false);
        result.put("success", false);
        result.put("candidates", Collections.emptyList());
        result.put("message", message);
        return result;
    }

    private Map<String, Object> buildSuccessResult(NavIntent intent, List<NavIntent> allIntents,
            String query, int confidence, String matchType) {
        Map<String, Object> result = new HashMap<>();
        
        List<MatchResult> results = new ArrayList<>();
        results.add(new MatchResult(allIntents.indexOf(intent), confidence, confidence, matchType));
        
        result.put("matched", true);
        result.put("success", true);
        result.put("candidates", buildCandidates(results, allIntents, query, result));
        result.put("bestMatch", ((List<Map<String, Object>>) result.get("candidates")).get(0));
        result.put("matchType", matchType);
        
        copyBestMatch(result, ((List<Map<String, Object>>) result.get("candidates")).get(0));
        return result;
    }

    private void copyBestMatch(Map<String, Object> result, Map<String, Object> best) {
        result.put("intentId", best.get("intentId"));
        result.put("intentCode", best.get("intentCode"));
        result.put("intentName", best.get("intentName"));
        result.put("targetName", best.get("intentName"));
        result.put("targetPath", best.get("targetPath"));
        result.put("confidence", best.get("confidence"));
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return (start >= 0 && end > start) ? text.substring(start, end + 1) : "{}";
    }

    // 内部类：匹配结果
    private static class MatchResult {
        final int index;
        int score;
        double confidence;
        final String matchType;

        MatchResult(int index, int score, double confidence, String matchType) {
            this.index = index;
            this.score = score;
            this.confidence = confidence;
            this.matchType = matchType;
        }
    }

    @Override
    public Map<String, Object> syncMenusToIntents() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取所有启用的菜单（包括目录和菜单类型）
        // 注意：@TableLogic 会自动处理 deleted 条件，不需要手动添加
        log.info("开始查询菜单数据...");
        
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getStatus, 1)
                .in(SysMenu::getMenuType, 0, 1) // 目录(0)和菜单(1)类型
                .isNotNull(SysMenu::getPath) // 必须有路径
                .ne(SysMenu::getPath, "") // 路径不能为空字符串
                .orderByAsc(SysMenu::getSortOrder);
        
        List<SysMenu> menus = sysMenuMapper.selectList(wrapper);
        
        log.info("查询到 {} 条菜单数据", menus != null ? menus.size() : 0);
        if (menus != null && !menus.isEmpty()) {
            for (SysMenu menu : menus) {
                log.info("菜单: id={}, name={}, type={}, path={}", 
                    menu.getId(), menu.getMenuName(), menu.getMenuType(), menu.getPath());
            }
        }
        
        if (menus == null || menus.isEmpty()) {
            result.put("success", false);
            result.put("added", 0);
            result.put("updated", 0);
            result.put("total", 0);
            result.put("message", "没有找到可同步的菜单，请检查sys_menu表是否有数据（status=1, menu_type in (0,1), path不为空）");
            return result;
        }

        // 2. 全量同步：物理删除所有意图数据（绕过 @TableLogic 软删除）
        int deletedCount = baseMapper.physicalDeleteAll();
        log.info("已物理删除 nav_intent 表 {} 条记录，准备重新导入 {} 条菜单", deletedCount, menus.size());

        // 3. 构建菜单信息供LLM分析
        StringBuilder menuInfo = new StringBuilder();
        for (int i = 0; i < menus.size(); i++) {
            SysMenu menu = menus.get(i);
            menuInfo.append(i + 1).append(". ")
                    .append(menu.getMenuName())
                    .append(" | 路径: ").append(menu.getPath())
                    .append(" | 类型: ").append(menu.getMenuType() == 0 ? "目录" : "菜单")
                    .append("\n");
        }

        // 4. 调用LLM生成关键词和描述
        String systemPrompt = buildSyncPrompt();
        String userPrompt = "以下是需要生成导航意图的菜单列表：\n\n" + menuInfo;

        log.info("准备调用LLM生成关键词，菜单数量: {}", menus.size());
        log.debug("SystemPrompt: {}", systemPrompt);
        log.debug("UserPrompt 长度: {} 字符", userPrompt.length());

        int added = 0;
        List<String> errors = new ArrayList<>();

        try {
            log.info("开始调用 LLM...");
            String aiResponse = llmClient.chatJson(
                    List.of(Map.of("role", "user", "content", userPrompt)),
                    systemPrompt
            );
            log.info("LLM 调用完成，响应长度: {}", aiResponse != null ? aiResponse.length() : 0);

            String jsonStr = extractJson(aiResponse);
            log.info("提取JSON完成，长度: {}", jsonStr != null ? jsonStr.length() : 0);
            
            JSONObject parsed = JSON.parseObject(jsonStr);
            JSONArray intents = parsed.getJSONArray("intents");
            log.info("解析到 {} 个意图", intents != null ? intents.size() : 0);

            if (intents != null) {
                for (int i = 0; i < intents.size(); i++) {
                    try {
                        JSONObject intentJson = intents.getJSONObject(i);
                        int menuIndex = intentJson.getInteger("menuIndex") - 1;
                        String keywords = intentJson.getString("keywords");
                        String description = intentJson.getString("description");

                        if (menuIndex >= 0 && menuIndex < menus.size() && keywords != null) {
                            SysMenu menu = menus.get(menuIndex);
                            
                            // 新增意图
                            NavIntent newIntent = new NavIntent();
                            newIntent.setIntentCode(generateIntentCode(menu));
                            newIntent.setIntentName(menu.getMenuName());
                            newIntent.setKeywords(keywords);
                            newIntent.setTargetPath(menu.getPath());
                            newIntent.setDescription(description != null ? description : menu.getMenuName());
                            newIntent.setPriority(menus.indexOf(menu) + 1);
                            newIntent.setStatus(1);
                            newIntent.setCreatedBy("system");
                            newIntent.setCreatedTime(LocalDateTime.now());
                            save(newIntent);
                            added++;
                            log.info("新增意图: {} -> {}", menu.getMenuName(), keywords);
                        }
                    } catch (Exception e) {
                        errors.add("处理第 " + (i + 1) + " 个菜单失败: " + e.getMessage());
                        log.error("处理菜单失败", e);
                    }
                }
            }

            result.put("success", true);
            result.put("added", added);
            result.put("updated", 0);
            result.put("total", menus.size());
            result.put("errors", errors);
            result.put("message", String.format("全量同步完成！新增 %d 条意图，共处理 %d 个菜单", added, menus.size()));
            
        } catch (Exception e) {
            log.error("调用LLM同步菜单失败", e);
            result.put("success", false);
            result.put("added", 0);
            result.put("updated", 0);
            result.put("total", menus.size());
            result.put("message", "调用AI服务失败: " + e.getMessage());
        }

        return result;
    }

    private String generateIntentCode(SysMenu menu) {
        if (menu.getPermission() != null && !menu.getPermission().isEmpty()) {
            return menu.getPermission().replace(":", "_").replace(".", "_").toUpperCase();
        }
        if (menu.getMenuCode() != null && !menu.getMenuCode().isEmpty()) {
            return menu.getMenuCode().replace(":", "_").replace(".", "_").toUpperCase();
        }
        return "MENU_" + menu.getId();
    }

    private String buildSyncPrompt() {
        return "你是一个智能导航系统的关键词生成助手。\n\n"
                + "任务：根据菜单信息，为每个菜单生成合适的导航关键词和描述。\n\n"
                + "输出JSON格式：\n"
                + "{\n"
                + "  \"intents\": [\n"
                + "    {\n"
                + "      \"menuIndex\": 菜单序号(从1开始),\n"
                + "      \"keywords\": \"关键词1,关键词2,关键词3,常用说法1,常用说法2\",\n"
                + "      \"description\": \"功能简短描述\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n\n"
                + "关键词规则：\n"
                + "1. 包含菜单名称的核心词\n"
                + "2. 包含常见的用户搜索词\n"
                + "3. 包含同义词和相关词\n"
                + "4. 包含操作的动词组合（如新增、编辑、删除、查看）\n"
                + "5. 关键词之间用逗号分隔\n\n"
                + "描述规则：\n"
                + "1. 简洁明了，不超过50字\n"
                + "2. 说明该功能的主要作用";
    }
}
