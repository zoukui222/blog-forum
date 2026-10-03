package com.example.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI Agent 配置项
 * 对应 application.yml 中的 ai.* 配置；密钥不入版本库，通过 application-local.yml 或环境变量 AI_API_KEY 注入。
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 模型服务地址（DeepSeek 兼容 OpenAI Chat Completions 协议） */
    private String baseUrl = "https://api.deepseek.com";

    /** 密钥 */
    private String apiKey;

    /** 模型名 */
    private String model = "deepseek-chat";

    /** 建连超时(ms) */
    private Integer connectTimeout = 10000;

    /** 读超时(ms)：流式响应期间两个分片之间的最大间隔 */
    private Integer readTimeout = 120000;

    /** 单次对话最多允许的"工具调用轮次"，防止模型陷入死循环无限调用 */
    private Integer maxIterations = 5;

    /** 采样温度 */
    private Double temperature = 0.3;

    /** 单个工具返回给模型的最大字符数，超出截断，避免上下文爆炸 */
    private Integer toolResultLimit = 4000;

    /** 是否把工具调用明细写入审计表 */
    private Boolean auditEnabled = true;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getToolResultLimit() {
        return toolResultLimit;
    }

    public void setToolResultLimit(Integer toolResultLimit) {
        this.toolResultLimit = toolResultLimit;
    }

    public Boolean getAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(Boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }
}
