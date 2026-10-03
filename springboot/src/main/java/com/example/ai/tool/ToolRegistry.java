package com.example.ai.tool;

import com.example.ai.config.AiProperties;
import com.example.ai.core.AgentContext;
import com.example.ai.dto.AiToolCall;
import com.example.ai.dto.ToolInvokeResult;
import com.example.ai.entity.AiToolCallLog;
import com.example.ai.mapper.AiToolCallLogMapper;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表
 * 负责三件事：把 Spring 容器里所有 AiTool 汇总成模型可读的工具声明；按角色裁剪可见工具；
 * 执行工具并落审计日志（可观测性）。
 */
@Component
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Resource
    private List<AiTool> toolBeans;

    @Resource
    private AiProperties properties;

    @Resource
    private AiToolCallLogMapper toolCallLogMapper;

    /** 工具名 -> 工具实例，用 LinkedHashMap 保证声明顺序稳定（便于复现问题） */
    private final Map<String, AiTool> toolMap = new LinkedHashMap<String, AiTool>();

    @PostConstruct
    public void init() {
        for (AiTool tool : toolBeans) {
            if (toolMap.containsKey(tool.name())) {
                throw new IllegalStateException("工具名重复：" + tool.name());
            }
            toolMap.put(tool.name(), tool);
        }
        log.info("AI Agent 工具注册完成，共 {} 个：{}", toolMap.size(), toolMap.keySet());
    }

    public int size() {
        return toolMap.size();
    }

    /**
     * 生成 OpenAI / DeepSeek 格式的工具声明。
     * 按角色裁剪：管理员才能看到写工具，普通用户/游客连声明都拿不到，
     * 从源头上避免模型去尝试一个注定失败的调用。
     */
    public ArrayNode buildToolSpec(AgentContext context) {
        ArrayNode array = mapper.createArrayNode();
        for (AiTool tool : toolMap.values()) {
            if (tool.adminOnly() && (context == null || !context.isAdmin())) {
                continue;
            }
            ObjectNode item = array.addObject();
            item.put("type", "function");
            ObjectNode function = item.putObject("function");
            function.put("name", tool.name());
            function.put("description", tool.description());
            function.set("parameters", mapper.valueToTree(tool.parameters()));
        }
        return array.size() > 0 ? array : null;
    }

    /**
     * 执行一次工具调用。
     * 约定不向外抛异常：任何失败都转成可读文本回给模型，让它自己决定重试、改参数还是如实告诉用户，
     * 而不是把整个对话中断成 500。
     */
    public ToolInvokeResult invoke(AiToolCall call, AgentContext context, Integer chatLogId) {
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String content;

        AiTool tool = toolMap.get(call.getName());
        try {
            if (tool == null) {
                success = false;
                errorMsg = "工具不存在";
                content = "错误：不存在名为 " + call.getName() + " 的工具，请在可用工具范围内作答。";
            } else if (tool.adminOnly() && (context == null || !context.isAdmin())) {
                success = false;
                errorMsg = "权限不足";
                content = "拒绝执行：工具 " + call.getName() + " 需要管理员权限，当前账号无权调用。请如实告知用户。";
            } else {
                JsonNode args = parseArguments(call.getArguments());
                content = tool.execute(args, context);
                if (content == null) {
                    content = "（工具无返回内容）";
                }
            }
        } catch (Exception e) {
            success = false;
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            content = "工具执行异常：" + e.getMessage() + "。请换一种方式，或如实说明查询失败。";
            log.error("工具执行失败: tool={}, args={}", call.getName(), call.getArguments(), e);
        }

        long cost = System.currentTimeMillis() - start;

        // 结果截断后再回灌：一次工具可能返回几万字，不截断会直接把上下文撑爆
        String limited = abbreviate(content, properties.getToolResultLimit());

        // 审计：失败也不影响主流程，只记警告
        if (Boolean.TRUE.equals(properties.getAuditEnabled())) {
            try {
                AiToolCallLog record = new AiToolCallLog();
                record.setChatLogId(chatLogId);
                record.setToolName(call.getName());
                record.setArguments(abbreviate(call.getArguments(), 2000));
                record.setResult(abbreviate(limited, 2000));
                record.setSuccess(success ? 1 : 0);
                record.setErrorMsg(errorMsg);
                record.setDurationMs(cost);
                record.setCreateTime(DateUtil.now());
                toolCallLogMapper.insert(record);
            } catch (Exception auditError) {
                log.warn("写入工具审计日志失败，已忽略: {}", auditError.getMessage());
            }
        }

        log.info("工具调用完成: tool={}, success={}, cost={}ms", call.getName(), success, cost);
        return new ToolInvokeResult(success, limited, cost);
    }

    /** 模型偶发会给出空串或非法 JSON，这里兜底成空对象，让工具自己报"参数缺失" */
    private JsonNode parseArguments(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return mapper.createObjectNode();
        }
        try {
            return mapper.readTree(raw);
        } catch (Exception e) {
            log.warn("工具参数不是合法 JSON，按空参数处理: {}", raw);
            return mapper.createObjectNode();
        }
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        if (max <= 0 || text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "\n…（内容过长已截断）";
    }
}
