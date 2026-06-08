package com.example.evolutionary_ai_model.service.strategy;

import cn.hutool.core.util.IdUtil;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.dto.ChatMessageDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import com.example.evolutionary_ai_model.service.AiConversationService;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.service.factory.ProviderChatModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用法：动态模型对话策略实现类，根据两级配置和会话钉选动态创建ChatClient进行对话。
 * 支持会话级模型钉选，优先使用会话钉选的模型配置，确保"用户选什么用什么"。
 * 使用ProviderChatModelFactory根据协议类型动态构建ChatModel实例。
 */
@Component
public class DynamicChatStrategy implements ChatStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DynamicChatStrategy.class);

    // 策略标识
    private static final String MODE = "dynamic";

    // 模型配置服务
    @Autowired
    private AiModelConfigService modelConfigService;

    // 供应商配置服务
    @Autowired
    private AiProviderConfigService providerConfigService;

    // 会话服务
    @Autowired
    private AiConversationService conversationService;

    // Provider ChatModel工厂
    @Autowired
    private ProviderChatModelFactory chatModelFactory;

    @Override
    public String getMode() {
        return MODE;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        logger.info("动态模式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            // 获取模型配置（优先使用会话钉选）
            AiModelConfig modelConfig = getModelConfig(request);
            if (modelConfig == null) {
                throw new RuntimeException("未找到可用的模型配置");
            }

            // 获取供应商配置
            AiProviderConfig providerConfig = getProviderConfig(modelConfig);
            if (providerConfig == null) {
                throw new RuntimeException("未找到可用的供应商配置");
            }

            // 使用工厂创建ChatClient（根据协议类型动态构建）
            ChatClient chatClient = chatModelFactory.getOrCreateChatClient(providerConfig, modelConfig);

            // 构建提示词
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 调用AI模型
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.info("动态模式对话成功，模型配置ID: {}, 供应商配置ID: {}, 响应内容长度: {}", 
                    modelConfig.getId(), providerConfig.getId(), response.length());

            // 构建响应DTO
            return ChatResponseDTO.builder()
                    .conversationId(request.getConversationId() != null ? request.getConversationId() : IdUtil.fastSimpleUUID())
                    .messageId(IdUtil.fastSimpleUUID())
                    .content(response)
                    .mode(MODE)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            logger.error("动态模式对话异常", e);
            throw new RuntimeException("AI对话失败: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequestDTO request) {
        logger.info("动态模式流式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            // 获取模型配置（优先使用会话钉选）
            AiModelConfig modelConfig = getModelConfig(request);
            if (modelConfig == null) {
                return Flux.error(new RuntimeException("未找到可用的模型配置"));
            }

            // 获取供应商配置
            AiProviderConfig providerConfig = getProviderConfig(modelConfig);
            if (providerConfig == null) {
                return Flux.error(new RuntimeException("未找到可用的供应商配置"));
            }

            // 使用工厂创建ChatClient
            ChatClient chatClient = chatModelFactory.getOrCreateChatClient(providerConfig, modelConfig);

            // 构建提示词
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 流式调用AI模型
            return chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();

        } catch (Exception e) {
            logger.error("动态模式流式对话异常", e);
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

    /**
     * 获取模型配置（优先使用会话钉选）
     * 实现会话级钉选逻辑：用户在聊天界面选择模型后钉选到会话，后续该会话所有消息都用此模型
     * @param request 对话请求
     * @return 模型配置实体
     */
    private AiModelConfig getModelConfig(ChatRequestDTO request) {
        // 优先级1：如果指定了configId，使用指定的配置（显式选择绕过能力路由）
        if (request.getConfigId() != null) {
            logger.info("使用指定模型配置，配置ID: {}", request.getConfigId());
            return modelConfigService.getConfigById(request.getConfigId());
        }

        // 优先级2：如果有conversationId，检查会话是否钉选了模型
        if (request.getConversationId() != null) {
            Long pinnedConfigId = conversationService.getPinnedModelConfigId(request.getConversationId());
            if (pinnedConfigId != null) {
                logger.info("使用会话钉选的模型配置，配置ID: {}", pinnedConfigId);
                return modelConfigService.getConfigById(pinnedConfigId);
            }
        }

        // 优先级3：如果没有configId但有userId，获取用户的默认模型配置
        if (request.getUserId() != null) {
            logger.info("获取用户默认模型配置，用户ID: {}", request.getUserId());
            AiModelConfig defaultConfig = modelConfigService.getDefaultConfig(request.getUserId());
            if (defaultConfig != null) {
                logger.info("找到用户默认模型配置，配置ID: {}", defaultConfig.getId());
                return defaultConfig;
            }
        }

        // 既没有configId也没有userId，无法获取模型配置
        logger.warn("未指定模型配置ID且无法获取用户默认模型，请传入configId参数或确保用户已登录");
        throw new RuntimeException("未找到可用的模型配置，请添加模型配置或指定configId");
    }

    /**
     * 根据模型配置获取关联的供应商配置
     * @param modelConfig 模型配置
     * @return 供应商配置实体
     */
    private AiProviderConfig getProviderConfig(AiModelConfig modelConfig) {
        // 通过 providerConfigId 获取供应商配置
        if (modelConfig.getProviderConfigId() == null) {
            logger.warn("模型配置缺少供应商配置关联，配置ID: {}", modelConfig.getId());
            throw new RuntimeException("模型配置未关联供应商配置，请先创建供应商配置");
        }
        
        logger.info("通过providerConfigId获取供应商配置，配置ID: {}", modelConfig.getProviderConfigId());
        AiProviderConfig providerConfig = providerConfigService.getConfigById(modelConfig.getProviderConfigId());
        
        if (providerConfig == null) {
            logger.warn("供应商配置不存在，配置ID: {}", modelConfig.getProviderConfigId());
            throw new RuntimeException("供应商配置不存在");
        }
        
        return providerConfig;
    }
}