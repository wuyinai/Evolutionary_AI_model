package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.service.strategy.ChatStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用法：AI对话策略工厂，负责管理和创建不同的对话策略实例。
 * 采用工厂模式，通过策略注册机制支持动态扩展新的对话模式。
 * 新增策略只需实现ChatStrategy接口并注册到工厂即可使用。
 */
@Component
public class ChatStrategyFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChatStrategyFactory.class);

    // 策略注册表，存储所有可用的对话策略
    private final Map<String, ChatStrategy> strategyMap = new HashMap<>();

    /**
     * 构造函数，通过Spring依赖注入自动注册所有策略
     * @param strategies 所有ChatStrategy实现类的列表
     */
    public ChatStrategyFactory(List<ChatStrategy> strategies) {
        // 自动注册所有策略
        for (ChatStrategy strategy : strategies) {
            registerStrategy(strategy);
        }
        logger.info("已注册 {} 个对话策略", strategyMap.size());
    }

    /**
     * 注册对话策略
     * @param strategy 对话策略实例
     */
    public void registerStrategy(ChatStrategy strategy) {
        String mode = strategy.getMode();
        strategyMap.put(mode, strategy);
        logger.info("注册对话策略: {}", mode);
    }

    /**
     * 根据模式获取对应的对话策略
     * @param mode 对话模式标识
     * @return 对话策略实例
     * @throws IllegalArgumentException 当模式不存在时抛出异常
     */
    public ChatStrategy getStrategy(String mode) {
        ChatStrategy strategy = strategyMap.get(mode);
        if (strategy == null) {
            logger.warn("未找到对话策略: {}", mode);
            throw new IllegalArgumentException("不支持的对话模式: " + mode);
        }
        logger.debug("获取对话策略: {}", mode);
        return strategy;
    }

    /**
     * 检查模式是否支持
     * @param mode 对话模式标识
     * @return 是否支持该模式
     */
    public boolean isSupported(String mode) {
        return strategyMap.containsKey(mode);
    }

    /**
     * 获取所有支持的模式列表
     * @return 支持的模式列表
     */
    public List<String> getSupportedModes() {
        return List.copyOf(strategyMap.keySet());
    }
}