package com.example.ai.core;

import com.example.ai.exception.ClientDisconnectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把 Agent 事件翻译成 SSE 事件推给浏览器
 *
 * 两个易错点：
 * 1) data 必须带上 MediaType.APPLICATION_JSON，交给 Jackson 转换器输出 UTF-8。
 *    如果直接 send 一个 String，Spring 会走 StringHttpMessageConverter，其默认字符集是
 *    ISO-8859-1，中文回答会全部变成乱码。
 * 2) 客户端断开时 send 会抛 IOException，必须转成 ClientDisconnectedException 往上传，
 *    否则 Agent 会继续烧 token 跑完整个循环。
 */
public class SseAgentListener implements AgentEventListener {

    private static final Logger log = LoggerFactory.getLogger(SseAgentListener.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SseEmitter emitter;

    public SseAgentListener(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onMeta(int chatLogId, String model, List<String> tools) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("chatLogId", chatLogId);
        data.put("model", model);
        data.put("tools", tools);
        send("meta", data);
    }

    @Override
    public void onToolCall(String toolName, String arguments) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("name", toolName);
        data.put("arguments", arguments);
        send("tool_call", data);
    }

    @Override
    public void onToolResult(String toolName, boolean success, String preview, long durationMs) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("name", toolName);
        data.put("success", success);
        data.put("durationMs", durationMs);
        data.put("preview", preview == null || preview.length() <= 300 ? preview : preview.substring(0, 300) + "…");
        send("tool_result", data);
    }

    @Override
    public void onContent(String delta) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("text", delta);
        send("content", data);
    }

    @Override
    public void onDone(int iterations, int promptTokens, int completionTokens, long durationMs, int chatLogId) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("iterations", iterations);
        data.put("promptTokens", promptTokens);
        data.put("completionTokens", completionTokens);
        data.put("durationMs", durationMs);
        data.put("chatLogId", chatLogId);
        send("done", data);
        complete();
    }

    @Override
    public void onError(String message, int chatLogId) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("message", message == null ? "AI 服务异常" : message);
        data.put("chatLogId", chatLogId);
        send("error", data);
        complete();
    }

    private void send(String event, Map<String, Object> payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(MAPPER.writeValueAsString(payload), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.info("SSE 推送失败（通常为用户已断开）: event={}, reason={}", event, e.getMessage());
            throw new ClientDisconnectedException("SSE 客户端已断开: " + event);
        }
    }

    private void complete() {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("关闭 SSE 连接时异常: {}", e.getMessage());
        }
    }
}
