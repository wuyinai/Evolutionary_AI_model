package com.example.evolutionary_ai_model.service.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用法：工具注册中心，统一管理所有Agent工具。
 * 采用注册表模式，通过Spring依赖注入自动注册所有Tool实现类。
 * 位于工具层，提供工具查询、注册和获取功能，支持Agent动态调用工具。
 * 使用@Component注解注册为Spring Bean，作为AgentService的工具管理组件。
 */
@Component
public class ToolRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ToolRegistry.class);

    // 工具注册表（工具名称 -> 工具实例）
    private final Map<String, Tool> toolMap = new HashMap<>();

    /**
     * 构造函数，通过Spring依赖注入自动注册所有工具
     * @param tools 所有Tool实现类的列表
     */
    public ToolRegistry(List<Tool> tools) {
        // 自动注册所有工具
        for (Tool tool : tools) {
            registerTool(tool);
        }
        logger.info("已注册 {} 个Agent工具", toolMap.size());
    }

    /**
     * 注册工具
     * @param tool 工具实例
     */
    public void registerTool(Tool tool) {
        String name = tool.getName();
        toolMap.put(name, tool);
        logger.info("注册工具: {} - {}", name, tool.getDescription());
    }

    /**
     * 根据名称获取工具
     * @param name 工具名称
     * @return 工具实例，如果不存在返回null
     */
    public Tool getTool(String name) {
        Tool tool = toolMap.get(name);
        if (tool == null) {
            logger.warn("未找到工具: {}", name);
        }
        return tool;
    }

    /**
     * 获取所有工具列表
     * @return 所有工具实例的列表
     */
    public List<Tool> getAllTools() {
        return List.copyOf(toolMap.values());
    }

    /**
     * 获取所有工具名称列表
     * @return 所有工具名称的列表
     */
    public List<String> getAllToolNames() {
        return List.copyOf(toolMap.keySet());
    }

    /**
     * 检查工具是否存在
     * @param name 工具名称
     * @return 是否存在
     */
    public boolean hasTool(String name) {
        return toolMap.containsKey(name);
    }

    /**
     * 获取工具数量
     * @return 注册的工具数量
     */
    public int getToolCount() {
        return toolMap.size();
    }

    /**
     * 获取所有工具的描述信息（用于AI模型理解可用工具）
     * @return 工具描述信息字符串
     */
    public String getToolDescriptions() {
        StringBuilder descriptions = new StringBuilder();
        descriptions.append("可用工具列表：\n");

        for (Tool tool : getAllTools()) {
            descriptions.append("- ").append(tool.getName())
                    .append(": ").append(tool.getDescription())
                    .append("\n");
        }

        return descriptions.toString();
    }
}