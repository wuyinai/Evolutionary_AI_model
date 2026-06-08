package com.example.evolutionary_ai_model.service.strategy;

import cn.hutool.core.util.IdUtil;
import com.example.evolutionary_ai_model.entity.dto.ChatMessageDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用法：快速模式对话策略实现类，提供快速响应的AI对话服务。
 * 使用默认模型参数，适合简单问答场景。
 * 通过ChatClient调用Spring AI框架与AI模型交互。
 */
@Component
public class QuickChatStrategy implements ChatStrategy {
    private static final Logger logger = LoggerFactory.getLogger(QuickChatStrategy.class);

    private final ChatClient chatClient;

    // 快速模式标识
    private static final String MODE = "quick";

    public QuickChatStrategy(ChatClient.Builder chatClientBuilder) {
        // 构建ChatClient实例
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getMode() {
        return MODE;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        logger.info("快速模式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            // 构建提示词
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 调用AI模型
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.info("快速模式对话成功，响应内容长度: {}", response.length());

            // 构建响应DTO
            return ChatResponseDTO.builder()
                    .conversationId(request.getConversationId() != null ? request.getConversationId() : IdUtil.fastSimpleUUID())
                    .messageId(IdUtil.fastSimpleUUID())
                    .content(response)
                    .mode(MODE)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            logger.error("快速模式对话异常", e);
            throw new RuntimeException("AI对话失败: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequestDTO request) {
        logger.info("快速模式流式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 流式调用AI模型，返回Flux<String>
            return chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();

        } catch (Exception e) {
            logger.error("快速模式流式对话异常", e);
            return Flux.error(new RuntimeException("AI流式对话失败: " + e.getMessage()));
        }
    }

    @Override
    public String buildPrompt(String message, List<ChatMessageDTO> history) {
        StringBuilder promptBuilder = new StringBuilder();

        // 添加历史消息上下文（如果有）
        if (history != null && !history.isEmpty()) {
            promptBuilder.append("以下是之前的对话记录：\n");
            for (ChatMessageDTO msg : history) {
                promptBuilder.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
            promptBuilder.append("\n");
        }

        // 添加当前用户消息
        promptBuilder.append("user: ").append(message);

        return promptBuilder.toString();
    }
}