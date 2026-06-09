package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import com.example.evolutionary_ai_model.service.ChatService;
import com.example.evolutionary_ai_model.service.strategy.DynamicChatStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 用法：AI对话服务实现类，负责处理对话相关的业务逻辑。
 * 使用DynamicChatStrategy实现动态模型配置对话。
 * 位于业务逻辑层，协调动态模型配置和AI模型调用。
 */
@Service
public class ChatServiceImpl implements ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final DynamicChatStrategy dynamicChatStrategy;

    public ChatServiceImpl(DynamicChatStrategy dynamicChatStrategy) {
        this.dynamicChatStrategy = dynamicChatStrategy;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        logger.info("处理对话请求，消息长度: {}, configId: {}", 
                request.getMessage().length(), request.getConfigId());

        // 使用动态模型策略进行对话
        ChatResponseDTO response = dynamicChatStrategy.chat(request);

        logger.info("对话完成，生成消息ID: {}", response.getMessageId());

        return response;
    }

    @Override
    public Flux<String> streamChat(ChatRequestDTO request) {
        logger.info("处理流式对话请求，消息长度: {}, configId: {}", 
                request.getMessage().length(), request.getConfigId());

        // 使用动态模型策略进行流式对话
        return dynamicChatStrategy.streamChat(request);
    }
}