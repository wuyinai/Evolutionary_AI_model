package com.example.evolutionary_ai_model.annotation;

import java.lang.annotation.*;

/**
 * 用法：自定义操作日志注解，标注在 Controller 方法上用于自动记录操作日志。
 * 配合 OperationLogAspect 切面实现日志的自动采集与持久化。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @OperationLog("添加用户")
 * @PostMapping
 * public Result<Void> addUser(@RequestBody @Validated UserAddDTO dto) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述，如"添加用户"、"修改角色"等
     */
    String value() default "";

    /**
     * 排除的参数名列表，不记录这些参数到日志中（如密码、Token等敏感信息）
     */
    String[] excludeParams() default {"password", "passwd", "secret", "token"};
}
