-- ============================================================
-- AI Agent 功能增量脚本（可重复执行）
-- 依赖：无。与 blog-xs.sql 中的业务表互相独立，不动任何现有表。
-- ============================================================

-- 会话记录：一次提问一条，记录回答、工具轮次与 token 消耗
DROP TABLE IF EXISTS `ai_chat_log`;
CREATE TABLE `ai_chat_log` (
  `id`                int          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`           int          DEFAULT NULL COMMENT '提问用户ID',
  `username`          varchar(255) DEFAULT NULL COMMENT '提问用户名',
  `role`              varchar(64)  DEFAULT NULL COMMENT '提问时角色 ADMIN/USER',
  `question`          varchar(2000) DEFAULT NULL COMMENT '用户提问',
  `answer`            longtext     COMMENT '模型最终回答',
  `iterations`        int          DEFAULT 0 COMMENT '工具调用轮次',
  `model`             varchar(128) DEFAULT NULL COMMENT '模型名',
  `prompt_tokens`     int          DEFAULT 0 COMMENT '输入token',
  `completion_tokens` int          DEFAULT 0 COMMENT '输出token',
  `duration_ms`       bigint       DEFAULT 0 COMMENT '端到端耗时(ms)',
  `status`            varchar(32)  DEFAULT NULL COMMENT 'RUNNING/SUCCESS/ABORTED/ERROR',
  `error_msg`         varchar(1000) DEFAULT NULL COMMENT '失败原因',
  `create_time`       varchar(64)  DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_user` (`user_id`),
  KEY `idx_ai_chat_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话记录';

-- 工具调用审计：模型每一次工具调用一条，回答"它到底查了什么、看到了什么"
DROP TABLE IF EXISTS `ai_tool_call_log`;
CREATE TABLE `ai_tool_call_log` (
  `id`           int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  `chat_log_id`  int           DEFAULT NULL COMMENT '关联会话记录ID',
  `tool_name`    varchar(128)  DEFAULT NULL COMMENT '工具名',
  `arguments`    text          COMMENT '模型给出的入参JSON',
  `result`       text          COMMENT '工具返回内容（截断后）',
  `success`      tinyint       DEFAULT 1 COMMENT '1成功 0失败',
  `error_msg`    varchar(1000) DEFAULT NULL COMMENT '失败原因',
  `duration_ms`  bigint        DEFAULT 0 COMMENT '执行耗时(ms)',
  `create_time`  varchar(64)   DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_tool_chat` (`chat_log_id`),
  KEY `idx_ai_tool_name` (`tool_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI工具调用审计日志';
