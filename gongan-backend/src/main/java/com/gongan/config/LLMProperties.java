package com.gongan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LLMProperties {

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 嵌入模型
     */
    private String embeddingModel;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 超时时间（毫秒）
     */
    private Long timeout;
}
