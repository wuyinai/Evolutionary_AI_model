package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.dto.ChatResponseDTO;

/**
 * 用法：AI对话服务接口，定义对话相关的业务操作。
 * 位于业务逻辑层，负责协调策略工厂和AI模型调用。
 */
public interface ChatService {
    /**
     * 执行对话，根据模式调用对应的策略
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponseDTO chat(ChatRequestDTO request);

    /**
     * 获取支持的模式列表
     * @return 支持的模式列表
     */
    java.util.List<String> getSupportedModes();
}