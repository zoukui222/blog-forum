package com.example.common.annotation;

import java.lang.annotation.*;

/**
 * 接口角色限制注解
 * 标注在 Controller 类或方法上时，仅允许指定角色访问；
 * 未标注的接口只需登录态合法即可访问。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String value() default "ADMIN";
}
