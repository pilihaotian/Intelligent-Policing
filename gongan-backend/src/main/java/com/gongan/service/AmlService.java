package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.AmlCustomerDueDiligence;

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
     */
    Map<String, Object> aiAnalyzeDueDiligence(Long ddId);

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
}
