package com.example.ai.service;

import cn.hutool.core.date.DateUtil;
import com.example.ai.client.DeepSeekClient;
import com.example.ai.config.AiProperties;
import com.example.ai.core.AgentContext;
import com.example.ai.core.AgentEventListener;
import com.example.ai.dto.AiMessage;
import com.example.ai.dto.AiToolCall;
import com.example.ai.dto.StreamResult;
import com.example.ai.dto.ToolInvokeResult;
import com.example.ai.entity.AiChatLog;
import com.example.ai.exception.ClientDisconnectedException;
import com.example.ai.mapper.AiChatLogMapper;
import com.example.ai.tool.ToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 主循环（ReAct 风格的 function calling 循环）
 *
 * 一轮完整流程：
 *   模型思考 → 若决定调用工具，则挂起等待 → 执行工具 → 把结果作为 tool 消息塞回对话历史 → 再次请求模型 → …
 *   直到模型不再要求调用工具、直接给出文本回答为止。
 *
 * 两道硬约束：
 *   1) 最大轮次熔断：模型可能反复调同一个工具（尤其在查询结果为空时），必须有上限；
 *   2) 收口兜底：触达上限时再补一次"不带工具"的请求，逼模型用已有信息作答，避免给用户一个空白回复。
 */
@Service
public class AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentService.class);

    private static final int QUESTION_LIMIT = 2000;

    @Resource
    private DeepSeekClient deepSeekClient;

    @Resource
    private ToolRegistry toolRegistry;

    @Resource
    private AiProperties properties;

    @Resource
    private AiChatLogMapper chatLogMapper;

    /**
     * 执行一次完整的 Agent 对话。方法本身不抛异常，所有异常都通过 listener 上报，
     * 保证 SSE 连接一定能收到结束事件（前端不会一直转圈）。
     */
    public void run(String question, AgentContext context, AgentEventListener listener) {
        long start = System.currentTimeMillis();
        int chatLogId = createChatLog(question, context);

        int promptTokens = 0;
        int completionTokens = 0;
        int iterations = 0;
        StringBuilder answer = new StringBuilder();
        String status = "SUCCESS";
        String errorMsg = null;

        try {
            List<AiMessage> messages = new ArrayList<AiMessage>();
            messages.add(AiMessage.system(buildSystemPrompt(context)));
            messages.add(AiMessage.user(question));

            ArrayNode tools = toolRegistry.buildToolSpec(context);
            listener.onMeta(chatLogId, properties.getModel(), toolNamesOf(tools));

            int maxRounds = properties.getMaxIterations() == null ? 5 : properties.getMaxIterations();

            for (int round = 0; round < maxRounds; round++) {
                StreamResult result = deepSeekClient.chatStream(messages, tools, listener::onContent);
                promptTokens += result.getPromptTokens();
                completionTokens += result.getCompletionTokens();

                if (!result.hasToolCalls()) {
                    answer.append(result.getContent());
                    break;
                }

                iterations++;

                // 关键：把模型这一轮的"我要调这些工具"原样写回历史，
                // 否则下一轮请求里出现 tool 结果却找不到对应的 tool_calls，接口会直接报 400
                AiMessage assistantMsg = AiMessage.assistant(result.getContent());
                assistantMsg.setToolCalls(result.getToolCalls());
                messages.add(assistantMsg);

                for (AiToolCall call : result.getToolCalls()) {
                    listener.onToolCall(call.getName(), call.getArguments());
                    ToolInvokeResult invokeResult = toolRegistry.invoke(call, context, chatLogId);
                    listener.onToolResult(call.getName(), invokeResult.isSuccess(),
                            invokeResult.getContent(), invokeResult.getDurationMs());
                    messages.add(AiMessage.tool(call.getId(), call.getName(), invokeResult.getContent()));
                }

                if (round == maxRounds - 1) {
                    status = "ABORTED";
                    errorMsg = "达到最大工具调用轮次 " + maxRounds;
                    log.warn("Agent 触达工具轮次上限: chatLogId={}, question={}", chatLogId, question);
                    // 收口：去掉工具再问一次，让模型基于已经拿到的事实作答
                    StreamResult closing = deepSeekClient.chatStream(messages, null, listener::onContent);
                    promptTokens += closing.getPromptTokens();
                    completionTokens += closing.getCompletionTokens();
                    answer.append(closing.getContent());
                }
            }
        } catch (ClientDisconnectedException disconnected) {
            // 用户关了页面，没必要再落库一条"失败"记录
            log.info("客户端已断开，终止 Agent 执行: chatLogId={}", chatLogId);
            return;
        } catch (Exception e) {
            status = "ERROR";
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("Agent 执行失败: chatLogId={}, question={}", chatLogId, question, e);
        }

        long duration = System.currentTimeMillis() - start;
        String finalAnswer = answer.length() > 0 ? answer.toString() : "抱歉，本次没有生成有效回答，请换个说法再试一次。";

        finishChatLog(chatLogId, finalAnswer, iterations, promptTokens, completionTokens, duration, status, errorMsg);

        if ("ERROR".equals(status)) {
            listener.onError(errorMsg, chatLogId);
        } else {
            listener.onDone(iterations, promptTokens, completionTokens, duration, chatLogId);
        }
    }

    /**
     * 系统提示词。核心是把"不许编造"写成硬规则——工具型 Agent 最大的翻车点就是模型宁可编一个
     * 文章 ID 也不肯说"没查到"，必须在提示词层面压住。
     */
    private String buildSystemPrompt(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是个人博客站点「博客论坛」的智能助手，可以通过调用工具查询站内真实数据。\n\n");
        sb.append("必须遵守的规则：\n");
        sb.append("1. 凡是涉及站内文章、数量、统计、作者的问题，必须先调用工具拿到真实数据再回答；");
        sb.append("严禁凭空编造文章标题、文章 ID、阅读量或任何数字。\n");
        sb.append("2. 引用文章时给出标题和 id，方便用户点击定位。\n");
        sb.append("3. 工具返回为空或失败时，如实说明查询不到，不要用想象的内容填补。\n");
        sb.append("4. 与站点数据无关的问题（闲聊、通用技术问答）可以直接回答，但不要伪装成站内内容。\n");
        sb.append("5. 用简体中文回答，直接、简洁，不要客套话和免责声明。\n");
        sb.append("6. 用户明确要求创建/发布文章时，必须调用创建工具真正完成创建，");
        sb.append("不要把正文只写出来给用户看；创建成功后回报文章 id 与标题。\n");
        sb.append("7. 为确认是否撞题而做的检索最多一次，不要反复搜索同一主题。\n");

        if (context != null && context.isAuthenticated()) {
            String display = context.getName() == null ? context.getUsername() : context.getName();
            sb.append("\n当前登录用户：").append(display).append("，角色：").append(context.getRole()).append("。");
            if (context.isAdmin()) {
                sb.append("该用户是管理员，可以在用户明确要求时调用创建文章的工具。");
            } else {
                sb.append("该用户是普通用户，没有创建文章的权限；若用户提出发文章，请说明需要管理员操作。");
            }
        } else {
            sb.append("\n当前用户未登录（游客），只能查询公开信息。");
        }
        return sb.toString();
    }

    private List<String> toolNamesOf(ArrayNode tools) {
        List<String> names = new ArrayList<String>();
        if (tools == null) {
            return names;
        }
        for (JsonNode node : tools) {
            JsonNode fn = node.get("function");
            if (fn != null && fn.get("name") != null) {
                names.add(fn.get("name").asText());
            }
        }
        return names;
    }

    /** 先落一条 RUNNING 记录拿到主键，工具审计日志才有 chatLogId 可关联 */
    private int createChatLog(String question, AgentContext context) {
        try {
            AiChatLog record = new AiChatLog();
            if (context != null) {
                record.setUserId(context.getUserId());
                record.setUsername(context.getUsername());
                record.setRole(context.getRole());
            }
            record.setQuestion(abbreviate(question, QUESTION_LIMIT));
            record.setAnswer("");
            record.setIterations(0);
            record.setModel(properties.getModel());
            record.setPromptTokens(0);
            record.setCompletionTokens(0);
            record.setDurationMs(0L);
            record.setStatus("RUNNING");
            record.setCreateTime(DateUtil.now());
            chatLogMapper.insert(record);
            return record.getId() == null ? -1 : record.getId();
        } catch (Exception e) {
            log.warn("写入会话记录失败，本次对话将不留痕: {}", e.getMessage());
            return -1;
        }
    }

    private void finishChatLog(int chatLogId, String answer, int iterations, int promptTokens,
                               int completionTokens, long duration, String status, String errorMsg) {
        if (chatLogId <= 0) {
            return;
        }
        try {
            AiChatLog record = new AiChatLog();
            record.setId(chatLogId);
            record.setAnswer(answer);
            record.setIterations(iterations);
            record.setPromptTokens(promptTokens);
            record.setCompletionTokens(completionTokens);
            record.setDurationMs(duration);
            record.setStatus(status);
            record.setErrorMsg(errorMsg);
            chatLogMapper.updateResult(record);
        } catch (Exception e) {
            log.warn("回填会话记录失败: {}", e.getMessage());
        }
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }

    /** 供健康检查/前端展示当前可用工具 */
    public Map<String, Object> describeTools(AgentContext context) {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("model", properties.getModel());
        info.put("registered", toolRegistry.size());
        ArrayNode spec = toolRegistry.buildToolSpec(context);
        info.put("available", spec == null ? 0 : spec.size());
        return info;
    }
}
