package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import reactor.core.publisher.Flux;

/**
 * 用法：AI对话服务接口，定义对话相关的业务操作。
 * 位于业务逻辑层，负责协调动态模型配置和AI模型调用。
 */
public interface ChatService {
    /**
     * 流式对话，支持实时返回AI回复内容
     * @param request 对话请求
     * @return 流式响应内容（Flux<String>）
     */
    Flux<String> streamChat(ChatRequestDTO request);
}