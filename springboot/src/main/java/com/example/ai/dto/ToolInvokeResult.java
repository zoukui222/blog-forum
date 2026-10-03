package com.example.ai.dto;

/**
 * 一次工具调用的执行结果
 */
public class ToolInvokeResult {

    private final boolean success;
    private final String content;
    private final long durationMs;

    public ToolInvokeResult(boolean success, String content, long durationMs) {
        this.success = success;
        this.content = content;
        this.durationMs = durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getContent() {
        return content;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
