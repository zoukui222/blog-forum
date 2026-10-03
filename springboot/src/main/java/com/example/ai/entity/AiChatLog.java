package com.example.ai.entity;

/**
 * AI 会话记录
 * 每次用户提问存一条，记录本轮问答、工具调用轮次与 token 消耗，用于回溯与成本核算。
 */
public class AiChatLog {

    private Integer id;
    /** 提问者 ID */
    private Integer userId;
    /** 提问者用户名 */
    private String username;
    /** 提问者角色（ADMIN/USER） */
    private String role;
    /** 用户提问 */
    private String question;
    /** 模型最终回答 */
    private String answer;
    /** 本轮实际发生的工具调用轮次 */
    private Integer iterations;
    /** 使用的模型名 */
    private String model;
    /** 累计输入 token */
    private Integer promptTokens;
    /** 累计输出 token */
    private Integer completionTokens;
    /** 端到端耗时(ms) */
    private Long durationMs;
    /** SUCCESS / ERROR */
    private String status;
    /** 失败原因 */
    private String errorMsg;
    /** 创建时间 */
    private String createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
