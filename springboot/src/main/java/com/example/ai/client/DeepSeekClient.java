package com.example.ai.client;

import com.example.ai.config.AiProperties;
import com.example.ai.dto.AiMessage;
import com.example.ai.dto.AiToolCall;
import com.example.ai.dto.StreamResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 流式对话客户端
 *
 * 为什么不用 Hutool 的 HttpUtil：Hutool 的 execute() 会把响应体读完整才返回，SSE 长连接会一直阻塞，
 * 前端拿不到打字机效果。这里直接用 JDK 自带的 HttpURLConnection 自己读 InputStream，逐行解析 SSE，
 * 且不引入任何新依赖（项目 java.version=1.8，用不了 JDK11 的 java.net.http.HttpClient）。
 */
@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 文本增量回调：每收到一段正文就往外推一次，实现流式输出 */
    public interface DeltaListener {
        void onContent(String delta);
    }

    @Resource
    private AiProperties properties;

    /**
     * 发起一次流式对话，返回聚合后的结果（正文 + 待执行的工具调用）
     *
     * @param messages 完整对话历史（含上一轮的工具执行结果）
     * @param tools    OpenAI 格式的工具（function）声明，无工具传 null
     * @param listener 正文增量回调，可为 null
     */
    public StreamResult chatStream(List<AiMessage> messages, ArrayNode tools, DeltaListener listener) throws IOException {
        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException("AI 服务未配置密钥：请在 application-local.yml 中设置 ai.api-key，或注入环境变量 AI_API_KEY");
        }

        ObjectNode body = buildRequestBody(messages, tools);
        byte[] payload = MAPPER.writeValueAsBytes(body);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(properties.getBaseUrl() + "/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(properties.getConnectTimeout());
            conn.setReadTimeout(properties.getReadTimeout());
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("Authorization", "Bearer " + properties.getApiKey());

            OutputStream out = conn.getOutputStream();
            out.write(payload);
            out.flush();
            out.close();

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String err = readQuietly(conn.getErrorStream());
                log.error("DeepSeek 接口返回异常: status={}, body={}", code, err);
                throw new IllegalStateException("AI 服务返回异常状态 " + code + "：" + abbreviate(err, 300));
            }
            return parseSse(conn.getInputStream(), listener);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 构造请求体。
     * 注意 assistant 消息里若带 tool_calls，content 需要显式置 null（模型要求字段存在），
     * 否则部分网关会返回 400。
     */
    private ObjectNode buildRequestBody(List<AiMessage> messages, ArrayNode tools) {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", properties.getModel());
        body.put("stream", true);
        body.put("temperature", properties.getTemperature());

        ArrayNode msgArr = body.putArray("messages");
        for (AiMessage m : messages) {
            ObjectNode node = msgArr.addObject();
            node.put("role", m.getRole());

            if ("tool".equals(m.getRole())) {
                node.put("content", m.getContent() == null ? "" : m.getContent());
                node.put("tool_call_id", m.getToolCallId());
                if (m.getName() != null) {
                    node.put("name", m.getName());
                }
                continue;
            }

            if (m.getContent() == null) {
                node.putNull("content");
            } else {
                node.put("content", m.getContent());
            }

            if (m.getToolCalls() != null && !m.getToolCalls().isEmpty()) {
                ArrayNode tcArr = node.putArray("tool_calls");
                for (AiToolCall tc : m.getToolCalls()) {
                    ObjectNode tcNode = tcArr.addObject();
                    tcNode.put("id", tc.getId());
                    tcNode.put("type", "function");
                    ObjectNode fn = tcNode.putObject("function");
                    fn.put("name", tc.getName());
                    fn.put("arguments", tc.getArguments() == null ? "{}" : tc.getArguments());
                }
            }
        }

        if (tools != null && tools.size() > 0) {
            body.set("tools", tools);
        }
        return body;
    }

    /**
     * 解析 SSE 数据流。
     * 重点：tool_calls 在流式响应里是按 index 分片下发的，第一片带 id 和 function.name，
     * 后续片只带 function.arguments 的字符串片段，必须按 index 聚合并把 arguments 逐片拼接，
     * 拼完才是合法 JSON——这是流式 function calling 最容易踩的坑。
     */
    private StreamResult parseSse(InputStream in, DeltaListener listener) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        Map<Integer, AiToolCall> callMap = new LinkedHashMap<Integer, AiToolCall>();
        Map<Integer, StringBuilder> argBuf = new LinkedHashMap<Integer, StringBuilder>();
        String finishReason = null;
        int promptTokens = 0;
        int completionTokens = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || !line.startsWith("data:")) {
                continue; // 空行分隔符、event:、:keep-alive 注释行一律跳过
            }
            String data = line.substring(5).trim();
            if ("[DONE]".equals(data)) {
                break;
            }

            JsonNode node;
            try {
                node = MAPPER.readTree(data);
            } catch (Exception parseError) {
                log.warn("忽略无法解析的 SSE 分片: {}", abbreviate(data, 200));
                continue;
            }

            JsonNode usage = node.get("usage");
            if (usage != null && !usage.isNull()) {
                promptTokens = usage.path("prompt_tokens").asInt(promptTokens);
                completionTokens = usage.path("completion_tokens").asInt(completionTokens);
            }

            JsonNode choices = node.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                continue;
            }
            JsonNode choice = choices.get(0);

            JsonNode reason = choice.get("finish_reason");
            if (reason != null && !reason.isNull()) {
                finishReason = reason.asText();
            }

            JsonNode delta = choice.get("delta");
            if (delta == null || delta.isNull()) {
                continue;
            }

            JsonNode contentNode = delta.get("content");
            if (contentNode != null && !contentNode.isNull()) {
                String text = contentNode.asText();
                if (text != null && !text.isEmpty()) {
                    content.append(text);
                    if (listener != null) {
                        listener.onContent(text);
                    }
                }
            }

            JsonNode toolCalls = delta.get("tool_calls");
            if (toolCalls != null && toolCalls.isArray()) {
                for (JsonNode tc : toolCalls) {
                    int index = tc.path("index").asInt(0);
                    AiToolCall acc = callMap.get(index);
                    if (acc == null) {
                        acc = new AiToolCall();
                        callMap.put(index, acc);
                        argBuf.put(index, new StringBuilder());
                    }
                    JsonNode idNode = tc.get("id");
                    if (idNode != null && !idNode.isNull() && acc.getId() == null) {
                        acc.setId(idNode.asText());
                    }
                    JsonNode fn = tc.get("function");
                    if (fn != null && !fn.isNull()) {
                        JsonNode nameNode = fn.get("name");
                        if (nameNode != null && !nameNode.isNull() && acc.getName() == null) {
                            acc.setName(nameNode.asText());
                        }
                        JsonNode argNode = fn.get("arguments");
                        if (argNode != null && !argNode.isNull()) {
                            argBuf.get(index).append(argNode.asText());
                        }
                    }
                }
            }
        }
        reader.close();

        List<AiToolCall> toolCalls = new ArrayList<AiToolCall>();
        for (Map.Entry<Integer, AiToolCall> entry : callMap.entrySet()) {
            AiToolCall call = entry.getValue();
            StringBuilder buf = argBuf.get(entry.getKey());
            call.setArguments(buf == null ? "{}" : buf.toString());
            toolCalls.add(call);
        }

        StreamResult result = new StreamResult();
        result.setContent(content.toString());
        result.setToolCalls(toolCalls);
        result.setFinishReason(finishReason);
        result.setPromptTokens(promptTokens);
        result.setCompletionTokens(completionTokens);
        return result;
    }

    private String readQuietly(InputStream in) {
        if (in == null) {
            return "";
        }
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while ((len = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, len);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
                // 读取失败时的关闭异常无需处理
            }
        }
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
