package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

/**
 * 用法：AI对话服务接口，定义对话相关的业务操作。
 * 位于业务逻辑层，负责协调动态模型配置和AI模型调用。
 */
public interface ChatService {
    /**
     * 执行对话，使用动态模型配置
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponseDTO chat(ChatRequestDTO request);

    /**
     * 流式对话，支持实时返回AI回复内容
     * @param request 对话请求
     * @return 流式响应内容（Flux<String>）
     */
    Flux<String> streamChat(ChatRequestDTO request);
}