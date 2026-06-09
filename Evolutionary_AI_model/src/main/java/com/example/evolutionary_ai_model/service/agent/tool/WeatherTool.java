package com.example.evolutionary_ai_model.service.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;

/**
 * 用法：天气查询工具，提供模拟的天气查询功能。
 * 用于Agent处理天气查询任务，模拟返回天气信息。
 * 位于工具层，封装天气查询逻辑，支持Agent自动调用。
 * 使用@Component注解注册为Spring Bean，通过ToolRegistry统一管理。
 * 同时通过@Bean方法注册为Function，供Spring AI自动调用。
 * 注意：当前为模拟实现，实际应用中可对接真实天气API。
 */
@Component
public class WeatherTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(WeatherTool.class);

    @Override
    public String getName() {
        return "weather";
    }

    @Override
    public String getDescription() {
        return "天气查询工具，查询指定城市的天气信息。输入城市名称，返回天气状况。";
    }

    @Override
    public String execute(Map<String, Object> params) {
        logger.info("执行天气查询工具，参数: {}", params);

        try {
            // 获取城市参数（支持city和input两种参数名）
            String city = (String) params.get("city");
            if (city == null) {
                city = (String) params.get("input"); // 支持input参数名
            }
            if (city == null || city.isEmpty()) {
                logger.warn("天气查询工具缺少city参数");
                return "错误：缺少city参数";
            }

            // 模拟天气查询结果（实际应用中可对接真实天气API）
            String result = simulateWeatherQuery(city);
            logger.info("天气查询结果: {}", result);

            return result;

        } catch (Exception e) {
            logger.error("天气查询工具执行失败", e);
            return "错误：" + e.getMessage();
        }
    }

    @Override
    public String getSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "city": {
                  "type": "string",
                  "description": "城市名称，如: 北京、上海、广州"
                }
              },
              "required": ["city"]
            }
            """;
    }

    /**
     * 注册为Spring AI的Function Bean
     * @return Function实例
     */
    @Bean("weather")
    public Function<String, String> weatherFunction() {
        return input -> {
            logger.info("Spring AI调用天气查询工具，输入: {}", input);
            return simulateWeatherQuery(input);
        };
    }

    /**
     * 模拟天气查询功能（简化实现）
     * @param city 城市名称
     * @return 模拟的天气信息
     */
    private String simulateWeatherQuery(String city) {
        // 模拟返回天气信息
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder result = new StringBuilder();
        result.append("天气查询结果（模拟）：\n");
        result.append("城市: ").append(city).append("\n");
        result.append("查询时间: ").append(now.format(formatter)).append("\n");
        result.append("天气状况: 晴朗\n");
        result.append("温度: 25°C\n");
        result.append("湿度: 60%\n");
        result.append("风速: 3级\n");
        result.append("提示：当前为模拟天气查询，实际应用中可对接真实天气API。");

        return result.toString();
    }
}