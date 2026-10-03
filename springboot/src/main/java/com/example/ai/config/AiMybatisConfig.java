package com.example.ai.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 让 MyBatis 扫描 AI 模块自己的 Mapper 包。
 * 主类的 @MapperScan("com.example.mapper") 不会覆盖到 com.example.ai.mapper，
 * 这里独立注册一个，避免改动主类与现有 Mapper。
 */
@Configuration
@MapperScan("com.example.ai.mapper")
public class AiMybatisConfig {
}
