package com.example.ai.controller;

import com.example.ai.core.AgentContext;
import com.example.ai.core.SseAgentListener;
import com.example.ai.service.AiAgentService;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * AI Agent 接口
 *
 * 为什么用 GET + SseEmitter 而不是 POST：
 * 浏览器原生 EventSource 只支持 GET，且不能自定义请求头。
 * 项目里的 JwtInterceptor 已经支持从 URL 参数取 token，所以直接把 token 放在查询串里即可，
 * 无需为 AI 接口单独开一条鉴权通道。
 */
@RestController
@RequestMapping("/ai")
public class AiAgentController {

    private static final Logger log = LoggerFactory.getLogger(AiAgentController.class);

    /** 单次对话最长挂起时间；0 表示不超时，这里给个上限避免连接泄漏 */
    private static final long SSE_TIMEOUT_MS = 300000L;

    @Resource
    private AiAgentService aiAgentService;

    @Resource(name = "aiAgentExecutor")
    private ThreadPoolTaskExecutor aiAgentExecutor;

    /**
     * 流式对话。事件类型：meta / tool_call / tool_result / content / done / error
     */
    @GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestParam("message") String message) {
        // 关键：身份快照必须在当前请求线程里取。
        // TokenUtils 依赖 RequestContextHolder(ThreadLocal)，一旦切到 Agent 线程池就取不到了，
        // 所以这里先物化成 AgentContext，作为参数显式传下去，全程不再碰 ThreadLocal。
        Account currentUser = TokenUtils.getCurrentUser();
        final AgentContext context = new AgentContext(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getName(),
                currentUser.getRole());

        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        final String question = message == null ? "" : message.trim();

        if (question.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"message\":\"提问内容不能为空\"}", MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception ignored) {
                // 空提问直接结束，无需额外处理
            }
            return emitter;
        }

        emitter.onTimeout(new Runnable() {
            @Override
            public void run() {
                log.warn("AI 对话超时: userId={}", context.getUserId());
                emitter.complete();
            }
        });

        log.info("收到 AI 提问: userId={}, role={}, question={}", context.getUserId(), context.getRole(), question);

        aiAgentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                aiAgentService.run(question, context, new SseAgentListener(emitter));
            }
        });

        return emitter;
    }

    /** 查看当前账号可见的 Agent 配置（模型、已注册/可用工具数） */
    @GetMapping("/info")
    public Result info() {
        Account currentUser = TokenUtils.getCurrentUser();
        AgentContext context = new AgentContext(currentUser.getId(), currentUser.getUsername(),
                currentUser.getName(), currentUser.getRole());
        return Result.success(aiAgentService.describeTools(context));
    }
}
