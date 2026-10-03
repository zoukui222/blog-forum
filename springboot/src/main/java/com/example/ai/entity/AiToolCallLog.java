package com.example.ai.entity;

/**
 * AI 工具调用审计日志
 * 模型每一次工具调用都落一条：调了什么、参数是什么、返回什么、耗时多少、成功与否。
 * 这是 Agent 可观测性的基础——出问题时能直接回答"它到底查了什么、看到的是什么"。
 */
public class AiToolCallLog {

    private Integer id;
    /** 关联的会话记录 ID */
    private Integer chatLogId;
    /** 工具名 */
    private String toolName;
    /** 模型给出的入参（JSON） */
    private String arguments;
    /** 工具返回内容（超长会截断） */
    private String result;
    /** 1=成功 0=失败 */
    private Integer success;
    /** 失败原因 */
    private String errorMsg;
    /** 执行耗时(ms) */
    private Long durationMs;
    /** 创建时间 */
    private String createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChatLogId() {
        return chatLogId;
    }

    public void setChatLogId(Integer chatLogId) {
        this.chatLogId = chatLogId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
