package com.example.evolutionary_ai_model.service.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * 用法：搜索工具，提供模拟的搜索功能。
 * 用于Agent处理信息检索任务，模拟返回搜索结果。
 * 位于工具层，封装搜索逻辑，支持Agent自动调用。
 * 使用@Component注解注册为Spring Bean，通过ToolRegistry统一管理。
 * 同时通过@Bean方法注册为Function，供Spring AI自动调用。
 * 注意：当前为模拟实现，实际应用中可对接真实搜索引擎API。
 */
@Component
public class SearchTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(SearchTool.class);

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "搜索工具，根据关键词搜索相关信息。输入查询关键词，返回搜索结果摘要。";
    }

    @Override
    public String execute(Map<String, Object> params) {
        logger.info("执行搜索工具，参数: {}", params);

        try {
            // 获取查询参数（支持query和input两种参数名）
            String query = (String) params.get("query");
            if (query == null) {
                query = (String) params.get("input"); // 支持input参数名
            }
            if (query == null || query.isEmpty()) {
                logger.warn("搜索工具缺少query参数");
                return "错误：缺少query参数";
            }

            // 模拟搜索结果（实际应用中可对接真实搜索引擎API）
            String result = simulateSearch(query);
            logger.info("搜索结果: {}", result);

            return result;

        } catch (Exception e) {
            logger.error("搜索工具执行失败", e);
            return "错误：" + e.getMessage();
        }
    }

    @Override
    public String getSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "搜索关键词或查询语句"
                }
              },
              "required": ["query"]
            }
            """;
    }

    /**
     * 注册为Spring AI的Function Bean
     * @return Function实例
     */
    @Bean("search")
    public Function<String, String> searchFunction() {
        return input -> {
            logger.info("Spring AI调用搜索工具，输入: {}", input);
            return simulateSearch(input);
        };
    }

    /**
     * 模拟搜索功能（简化实现）
     * @param query 搜索关键词
     * @return 模拟的搜索结果
     */
    private String simulateSearch(String query) {
        // 模拟返回搜索结果
        StringBuilder result = new StringBuilder();
        result.append("搜索结果（模拟）：\n");
        result.append("关键词: ").append(query).append("\n");
        result.append("找到以下相关信息：\n");
        result.append("1. ").append(query).append(" 的基本概念和定义\n");
        result.append("2. ").append(query).append(" 的应用场景和案例\n");
        result.append("3. ").append(query).append(" 的最新发展和趋势\n");
        result.append("提示：当前为模拟搜索，实际应用中可对接真实搜索引擎API。");

        return result.toString();
    }
}