package com.example.evolutionary_ai_model.service.agent.tool;

import java.util.Map;

/**
 * 用法：Agent工具接口，定义Agent可调用的工具的基本规范。
 * 所有Agent工具必须实现此接口，提供工具名称、描述、执行方法和参数Schema。
 * 位于工具层，封装具体的工具执行逻辑，支持Agent自动调用。
 * 实现此接口的类需要使用@Component注解注册为Spring Bean。
 */
public interface Tool {

    /**
     * 获取工具名称（唯一标识）
     * @return 工具名称
     */
    String getName();

    /**
     * 获取工具描述（用于AI模型理解工具用途）
     * @return 工具描述
     */
    String getDescription();

    /**
     * 执行工具
     * @param params 工具参数（Map形式）
     * @return 工具执行结果（字符串形式）
     */
    String execute(Map<String, Object> params);

    /**
     * 获取工具参数Schema（JSON格式，用于AI模型理解参数结构）
     * @return 参数Schema字符串
     */
    String getSchema();
}