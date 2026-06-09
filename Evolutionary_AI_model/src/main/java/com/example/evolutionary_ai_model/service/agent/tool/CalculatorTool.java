package com.example.evolutionary_ai_model.service.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * 用法：计算器工具，提供基本的数学计算功能。
 * 支持加、减、乘、除等基本运算，用于Agent处理数值计算任务。
 * 位于工具层，封装计算逻辑，支持Agent自动调用。
 * 使用@Component注解注册为Spring Bean，通过ToolRegistry统一管理。
 * 同时通过@Bean方法注册为Function，供Spring AI自动调用。
 */
@Component
public class CalculatorTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorTool.class);

    @Override
    public String getName() {
        return "calculator";
    }

    @Override
    public String getDescription() {
        return "计算器工具，支持基本数学运算（加、减、乘、除）。输入表达式，返回计算结果。";
    }

    @Override
    public String execute(Map<String, Object> params) {
        logger.info("执行计算器工具，参数: {}", params);

        try {
            // 获取表达式参数（支持expression和input两种参数名）
            String expression = (String) params.get("expression");
            if (expression == null) {
                expression = (String) params.get("input"); // 支持input参数名
            }
            if (expression == null || expression.isEmpty()) {
                logger.warn("计算器工具缺少expression参数");
                return "错误：缺少expression参数";
            }

            // 解析并计算表达式（简化实现，支持基本运算）
            double result = evaluateExpression(expression);
            logger.info("计算结果: {} = {}", expression, result);

            return String.format("计算结果: %s = %.2f", expression, result);

        } catch (Exception e) {
            logger.error("计算器工具执行失败", e);
            return "错误：" + e.getMessage();
        }
    }

    @Override
    public String getSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "expression": {
                  "type": "string",
                  "description": "数学表达式，如: 2+3, 10*5, 100/4"
                }
              },
              "required": ["expression"]
            }
            """;
    }

    /**
     * 注册为Spring AI的Function Bean
     * @return Function实例
     */
    @Bean("calculator")
    public Function<String, String> calculatorFunction() {
        return input -> {
            logger.info("Spring AI调用计算器工具，输入: {}", input);
            // 简化处理：直接解析输入字符串作为表达式
            try {
                double result = evaluateExpression(input);
                return String.format("计算结果: %s = %.2f", input, result);
            } catch (Exception e) {
                logger.error("计算器工具执行失败", e);
                return "错误：" + e.getMessage();
            }
        };
    }

    /**
     * 解析并计算数学表达式（简化实现）
     * @param expression 数学表达式
     * @return 计算结果
     */
    private double evaluateExpression(String expression) {
        // 简化实现：支持基本运算
        expression = expression.replaceAll(" ", "");

        // 尝试解析并计算
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
            return Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
        } else if (expression.contains("/")) {
            String[] parts = expression.split("/");
            double divisor = Double.parseDouble(parts[1]);
            if (divisor == 0) {
                throw new ArithmeticException("除数不能为0");
            }
            return Double.parseDouble(parts[0]) / divisor;
        } else {
            // 尝试直接解析为数字
            return Double.parseDouble(expression);
        }
    }
}