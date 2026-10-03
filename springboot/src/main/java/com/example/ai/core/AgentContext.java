package com.example.ai.core;

/**
 * Agent 执行上下文
 *
 * 为什么不直接用 TokenUtils.getCurrentUser()：
 * TokenUtils 依赖 RequestContextHolder（底层是 ThreadLocal），只在处理 HTTP 请求的那条线程上有效。
 * Agent 为了做 SSE 流式输出必须把执行逻辑交给异步/线程池线程，工具一旦在那个线程里跑，
 * ThreadLocal 就是空的——TokenUtils 会静默返回一个空 Account，导致写操作丢失 userId、
 * 权限判断全部失效（不报错，但越权）。
 * 所以这里在 Controller 主线程里一次性把身份快照拿出来，显式随参数传下去，
 * 不依赖任何 ThreadLocal，天然线程安全。
 */
public class AgentContext {

    private final Integer userId;
    private final String username;
    private final String name;
    private final String role;

    public AgentContext(Integer userId, String username, String name, String role) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /** 是否已登录（空上下文代表游客） */
    public boolean isAuthenticated() {
        return userId != null;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "AgentContext{userId=" + userId + ", role=" + role + "}";
    }
}
