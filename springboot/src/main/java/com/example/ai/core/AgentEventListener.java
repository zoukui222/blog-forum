package com.example.ai.core;

import java.util.List;

/**
 * Agent 执行过程中的事件回调
 * 实现方负责把事件转成 SSE 推给前端；用回调而不是直接依赖 SseEmitter，
 * 是为了让 Agent 主循环与传输协议解耦（换成 WebSocket 或命令行调试都不用改主循环）。
 */
public interface AgentEventListener {

    /** 本轮可用工具列表 */
    void onMeta(int chatLogId, String model, List<String> tools);

    /** 模型决定调用某个工具（在真正执行之前触发，前端可以立刻显示"正在查询…"） */
    void onToolCall(String toolName, String arguments);

    /** 工具执行完毕 */
    void onToolResult(String toolName, boolean success, String preview, long durationMs);

    /** 最终回答的正文增量 */
    void onContent(String delta);

    /** 正常结束 */
    void onDone(int iterations, int promptTokens, int completionTokens, long durationMs, int chatLogId);

    /** 出错结束 */
    void onError(String message, int chatLogId);
}
