package com.example.ai.exception;

/**
 * 客户端已断开（关闭页面 / 取消请求）
 * 用于让 Agent 主循环立即停止后续的模型调用与工具执行，别再做无用功。
 */
public class ClientDisconnectedException extends RuntimeException {

    public ClientDisconnectedException(String message) {
        super(message);
    }
}
