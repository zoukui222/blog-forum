package com.example.ai.dto;

/**
 * 一次工具调用请求
 * 流式响应中，arguments 是分片到达的，需要按 index 聚合后才是完整 JSON 字符串。
 */
public class AiToolCall {

    /** 模型分配的唯一 ID，回填工具结果时必须原样带回 */
    private String id;
    /** 工具名 */
    private String name;
    /** 参数（JSON 字符串） */
    private String arguments;

    public AiToolCall() {
    }

    public AiToolCall(String id, String name, String arguments) {
        this.id = id;
        this.name = name;
        this.arguments = arguments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}
