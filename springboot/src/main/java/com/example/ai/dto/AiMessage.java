package com.example.ai.dto;

import java.util.List;

/**
 * 对话消息（对齐 OpenAI / DeepSeek Chat Completions 的 message 结构）
 * role 取值：system / user / assistant / tool
 */
public class AiMessage {

    private String role;
    private String content;
    /** assistant 消息中：模型决定要调用的工具列表 */
    private List<AiToolCall> toolCalls;
    /** role=tool 时：回填的是哪一次工具调用 */
    private String toolCallId;
    /** role=tool 时：工具名（便于人工排查日志） */
    private String name;

    public AiMessage() {
    }

    public AiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
    }

    public static AiMessage tool(String toolCallId, String name, String content) {
        AiMessage msg = new AiMessage("tool", content);
        msg.setToolCallId(toolCallId);
        msg.setName(name);
        return msg;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
