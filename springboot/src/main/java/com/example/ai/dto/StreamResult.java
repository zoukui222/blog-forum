package com.example.ai.dto;

import java.util.List;

/**
 * 一次流式响应的聚合结果
 */
public class StreamResult {

    /** 文本正文（流式分片已拼接） */
    private String content = "";
    /** 模型要求调用的工具（为空表示本轮是最终回答） */
    private List<AiToolCall> toolCalls;
    /** stop / tool_calls / length */
    private String finishReason;
    /** 本轮消耗的 token，用于统计与计费核对 */
    private int promptTokens;
    private int completionTokens;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<AiToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<AiToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }
}
