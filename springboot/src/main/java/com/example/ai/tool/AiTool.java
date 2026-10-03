package com.example.ai.tool;

import com.example.ai.core.AgentContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Agent 可调用工具的统一抽象
 * 新增一个能力 = 新增一个实现了本接口的 Spring Bean，工具注册表会自动发现，
 * 无需改动 Agent 主循环与协议拼装代码。
 */
public interface AiTool {

    /** 工具名：模型据此选择工具，只允许小写字母和下划线 */
    String name();

    /** 工具说明：模型据此判断什么时候该用它，要写清"什么场景用"而不是"这个接口做了什么" */
    String description();

    /**
     * 参数 JSON Schema（type=object 的部分）
     * 例：properties={keyword={type=string, description=...}}，required=["keyword"]
     */
    Map<String, Object> parameters();

    /** 是否仅管理员可用。只读工具为 false，会产生副作用的写工具必须为 true */
    boolean adminOnly();

    /**
     * 执行工具。
     * 约定：返回值是给模型看的字符串（可以是 JSON 或自然语言），不要抛异常——
     * 失败也要以"可读的失败原因"返回，让模型有机会自行纠正参数或换一种方式回答。
     */
    String execute(JsonNode args, AgentContext context);
}
