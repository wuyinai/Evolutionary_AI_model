package com.example.evolutionary_ai_model.service.strategy;

import com.example.evolutionary_ai_model.dto.ChatMessageDTO;
import com.example.evolutionary_ai_model.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用法：AI对话策略接口，定义不同对话模式的统一行为。
 * 采用策略模式，支持快速模式、专家模式等多种对话策略。
 * 新增对话模式只需实现此接口，并通过工厂注册即可。
 */
public interface ChatStrategy {
    /**
     * 获取策略名称（模式标识）
     * @return 模式名称，如 "quick"、"expert"
     */
    String getMode();

    /**
     * 执行对话，调用AI模型生成回复
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

    /**
     * 构建对话提示词，不同模式可定制不同的prompt格式
     * @param message 用户消息
     * @param history 历史消息列表
     * @return 构建后的提示词
     */
    String buildPrompt(String message, List<ChatMessageDTO> history);
}