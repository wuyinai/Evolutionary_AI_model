package com.example.evolutionary_ai_model.service.strategy;

import cn.hutool.core.util.IdUtil;
import com.example.evolutionary_ai_model.dto.ChatMessageDTO;
import com.example.evolutionary_ai_model.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 用法：专家模式对话策略实现类，提供深度分析的AI对话服务。
 * 使用更高级的模型参数和系统提示词，适合复杂问题分析场景。
 * 通过ChatClient调用Spring AI框架与AI模型交互。
 */
@Component
public class ExpertChatStrategy implements ChatStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ExpertChatStrategy.class);

    private final ChatClient chatClient;

    // 专家模式标识
    private static final String MODE = "expert";

    // 专家模式系统提示词
    private static final String SYSTEM_PROMPT = """
            你是一个专业的AI助手，具有深厚的专业知识和分析能力。
            请对用户的问题进行深入分析，提供详细、专业、有逻辑的回答。
            回答时请：
            1. 先分析问题的核心要点
            2. 提供详细的解释和推理过程
            3. 给出专业的建议或解决方案
            4. 如有必要，提供相关的参考资料或延伸思考
            """;

    public ExpertChatStrategy(ChatClient.Builder chatClientBuilder) {
        // 构建ChatClient实例
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getMode() {
        return MODE;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        logger.info("专家模式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            // 构建提示词
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 创建包含系统消息的Prompt
            List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));
            messages.add(new org.springframework.ai.chat.messages.UserMessage(prompt));

            Prompt aiPrompt = new Prompt(messages);

            // 调用AI模型
            String response = chatClient.prompt()
                    .messages(aiPrompt.getInstructions())
                    .call()
                    .content();

            logger.info("专家模式对话成功，响应内容长度: {}", response.length());

            // 构建响应DTO
            return ChatResponseDTO.builder()
                    .conversationId(request.getConversationId() != null ? request.getConversationId() : IdUtil.fastSimpleUUID())
                    .messageId(IdUtil.fastSimpleUUID())
                    .content(response)
                    .mode(MODE)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            logger.error("专家模式对话异常", e);
            throw new RuntimeException("AI对话失败: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequestDTO request) {
        logger.info("专家模式流式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 创建包含系统消息的Prompt
            List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));
            messages.add(new org.springframework.ai.chat.messages.UserMessage(prompt));

            Prompt aiPrompt = new Prompt(messages);

            // 流式调用AI模型，返回Flux<String>
            return chatClient.prompt()
                    .messages(aiPrompt.getInstructions())
                    .stream()
                    .content();

        } catch (Exception e) {
            logger.error("专家模式流式对话异常", e);
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
            promptBuilder.append("\n请基于以上对话记录，继续回答用户的新问题。\n\n");
        }

        // 添加当前用户消息
        promptBuilder.append(message);

        return promptBuilder.toString();
    }
}