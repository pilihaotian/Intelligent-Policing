package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.AmlCustomerDueDiligence;
import com.gongan.entity.AmlSuspiciousTransactionReport;

import java.util.List;
import java.util.Map;

/**
 * 反洗钱服务接口
 */
public interface AmlService extends IService<AmlCustomerDueDiligence> {

    /**
     * 分页查询人员尽调
     */
    PageResult<AmlCustomerDueDiligence> pageDueDiligence(Map<String, Object> params);

    /**
     * AI辅助尽调分析
     * @param ddId 尽调记录ID
     * @param force 是否强制重新生成
     */
    Map<String, Object> aiAnalyzeDueDiligence(Long ddId, Boolean force);

    /**
     * 可疑交易甄别
     */
    Map<String, Object> suspiciousTransactionScreening(Long customerId);

    /**
     * 生成可疑交易报告
     */
    String generateSuspiciousReport(Long reportId);

    /**
     * 分页查询可疑交易列表
     */
    PageResult<Map<String, Object>> pageSuspiciousList(Integer current, Integer size, String customerName, String status);

    // ========== 线索管理相关接口 ==========

    /**
     * 分页查询线索列表
     */
    PageResult<AmlSuspiciousTransactionReport> pageClueList(Integer current, Integer size, String customerName, String status);

    /**
     * 获取线索详情
     */
    AmlSuspiciousTransactionReport getClueById(Long id);

    /**
     * AI分析线索
     * @param clueId 线索ID
     * @param force 是否强制重新生成
     */
    Map<String, Object> aiAnalyzeClue(Long clueId, Boolean force);

    /**
     * 处理线索
     */
    void handleClue(Long clueId, String conclusion, String description);

    /**
     * 生成线索报告
     */
    String generateClueReport(Long clueId);
}
