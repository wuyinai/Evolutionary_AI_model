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
 * 用法：时间查询工具，提供当前时间查询功能。
 * 用于Agent处理时间查询任务，返回当前时间和日期信息。
 * 位于工具层，封装时间查询逻辑，支持Agent自动调用。
 * 使用@Component注解注册为Spring Bean，通过ToolRegistry统一管理。
 * 同时通过@Bean方法注册为Function，供Spring AI自动调用。
 */
@Component
public class TimeTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(TimeTool.class);

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public String getDescription() {
        return "时间查询工具，查询当前时间和日期信息。可选输入格式类型，返回对应格式的时间。";
    }

    @Override
    public String execute(Map<String, Object> params) {
        logger.info("执行时间查询工具，参数: {}", params);

        try {
            // 获取格式参数（可选，支持format和input两种参数名）
            String format = (String) params.get("format");
            if (format == null) {
                format = (String) params.get("input"); // 支持input参数名
            }
            if (format == null || format.isEmpty()) {
                format = "default";
            }

            // 获取当前时间并格式化
            LocalDateTime now = LocalDateTime.now();
            String result = formatTime(now, format);
            logger.info("时间查询结果: {}", result);

            return result;

        } catch (Exception e) {
            logger.error("时间查询工具执行失败", e);
            return "错误：" + e.getMessage();
        }
    }

    @Override
    public String getSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "format": {
                  "type": "string",
                  "description": "时间格式类型：default-默认格式，date-仅日期，time-仅时间，full-完整格式",
                  "enum": ["default", "date", "time", "full"]
                }
              },
              "required": []
            }
            """;
    }

    /**
     * 注册为Spring AI的Function Bean
     * @return Function实例
     */
    @Bean("time")
    public Function<String, String> timeFunction() {
        return input -> {
            logger.info("Spring AI调用时间查询工具，输入: {}", input);
            LocalDateTime now = LocalDateTime.now();
            // 简化处理：如果输入为空或null，使用默认格式
            String format = (input == null || input.isEmpty()) ? "default" : input;
            return formatTime(now, format);
        };
    }

    /**
     * 格式化时间
     * @param time 时间对象
     * @param format 格式类型
     * @return 格式化后的时间字符串
     */
    private String formatTime(LocalDateTime time, String format) {
        DateTimeFormatter formatter;
        StringBuilder result = new StringBuilder();

        result.append("当前时间查询结果：\n");

        switch (format) {
            case "date":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                result.append("日期: ").append(time.format(formatter));
                break;
            case "time":
                formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                result.append("时间: ").append(time.format(formatter));
                break;
            case "full":
                formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");
                result.append("完整时间: ").append(time.format(formatter));
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                result.append("时间: ").append(time.format(formatter));
                result.append("\n星期: ").append(getWeekDay(time));
                break;
        }

        return result.toString();
    }

    /**
     * 获取星期几
     * @param time 时间对象
     * @return 星期几字符串
     */
    private String getWeekDay(LocalDateTime time) {
        String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        return weekDays[time.getDayOfWeek().getValue() - 1];
    }
}