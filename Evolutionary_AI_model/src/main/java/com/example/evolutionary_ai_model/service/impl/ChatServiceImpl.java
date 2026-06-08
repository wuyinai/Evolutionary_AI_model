package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import com.example.evolutionary_ai_model.service.ChatService;
import com.example.evolutionary_ai_model.service.factory.ChatStrategyFactory;
import com.example.evolutionary_ai_model.service.strategy.ChatStrategy;
import com.example.evolutionary_ai_model.service.strategy.DynamicChatStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用法：AI对话服务实现类，负责处理对话相关的业务逻辑。
 * 依赖ChatStrategyFactory获取策略实例，通过策略模式实现不同模式的对话。
 * 位于业务逻辑层，协调策略工厂和AI模型调用。
 * 支持动态模型配置，当请求中包含configId时使用DynamicChatStrategy。
 */
@Service
public class ChatServiceImpl implements ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatStrategyFactory strategyFactory;
    private final DynamicChatStrategy dynamicChatStrategy;

    public ChatServiceImpl(ChatStrategyFactory strategyFactory, DynamicChatStrategy dynamicChatStrategy) {
        this.strategyFactory = strategyFactory;
        this.dynamicChatStrategy = dynamicChatStrategy;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        logger.info("处理对话请求，模式: {}, 消息长度: {}, configId: {}", 
                request.getMode(), request.getMessage().length(), request.getConfigId());

        // 如果指定了configId，使用动态模型策略
        if (request.getConfigId() != null) {
            logger.info("使用动态模型策略，configId: {}", request.getConfigId());
            return dynamicChatStrategy.chat(request);
        }

        // 验证模式是否支持
        if (!strategyFactory.isSupported(request.getMode())) {
            logger.warn("不支持的对话模式: {}", request.getMode());
            throw new IllegalArgumentException("不支持的对话模式: " + request.getMode());
        }

        // 获取对应的策略
        ChatStrategy strategy = strategyFactory.getStrategy(request.getMode());

        // 执行对话
        ChatResponseDTO response = strategy.chat(request);

        logger.info("对话完成，生成消息ID: {}", response.getMessageId());

        return response;
    }

    @Override
    public List<String> getSupportedModes() {
        List<String> modes = new java.util.ArrayList<>(strategyFactory.getSupportedModes());
        // 添加动态模式
        modes.add("dynamic");
        return modes;
    }
}