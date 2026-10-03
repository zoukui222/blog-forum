package com.example.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Agent 专用线程池
 *
 * SSE 是长连接，一次对话可能持续几十秒并全程占用线程。如果直接跑在 Tomcat 的请求线程上，
 * 少量并发就能把 Web 线程池占满，导致登录、列表等普通接口全部排队。
 * 这里把 Agent 执行隔离到独立线程池，并设置队列与拒绝策略，保证故障不扩散到主业务。
 */
@Configuration
public class AiExecutorConfig {

    @Bean(name = "aiAgentExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor aiAgentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ai-agent-");
        // 队列满时由提交线程自己执行，宁可拖慢新请求，也不静默丢弃用户的提问
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
