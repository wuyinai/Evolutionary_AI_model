package com.example.evolutionary_ai_model.service.strategy;

import cn.hutool.core.util.IdUtil;
import com.example.evolutionary_ai_model.entity.dto.ChatMessageDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用法：动态模型对话策略实现类，根据用户配置的模型动态创建ChatClient进行对话。
 * 支持用户自定义模型配置，通过configId指定使用的模型。
 * 使用缓存机制避免重复创建ChatClient实例，提高性能。
 */
@Component
public class DynamicChatStrategy implements ChatStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DynamicChatStrategy.class);

    // 策略标识
    private static final String MODE = "dynamic";

    // ChatClient缓存，避免重复创建
    private final Map<Long, ChatClient> clientCache = new ConcurrentHashMap<>();

    // 模型配置服务
    private final AiModelConfigService configService;

    public DynamicChatStrategy(AiModelConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String getMode() {
        return MODE;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        logger.info("动态模式对话请求，消息内容长度: {}", request.getMessage().length());

        try {
            // 获取模型配置
            AiModelConfig config = getModelConfig(request);
            if (config == null) {
                throw new RuntimeException("未找到可用的模型配置");
            }

            // 获取或创建ChatClient
            ChatClient chatClient = getOrCreateClient(config);

            // 构建提示词
            String prompt = buildPrompt(request.getMessage(), request.getHistory());

            // 调用AI模型
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.info("动态模式对话成功，配置ID: {}, 响应内容长度: {}", config.getId(), response.length());

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
            // 获取模型配置
            AiModelConfig config = getModelConfig(request);
            if (config == null) {
                return Flux.error(new RuntimeException("未找到可用的模型配置"));
            }

            // 获取或创建ChatClient
            ChatClient chatClient = getOrCreateClient(config);

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
     * 获取模型配置
     * @param request 对话请求
     * @return 模型配置实体
     */
    private AiModelConfig getModelConfig(ChatRequestDTO request) {
        // 如果指定了configId，使用指定的配置
        if (request.getConfigId() != null) {
            logger.info("使用指定模型配置，配置ID: {}", request.getConfigId());
            return configService.getConfigById(request.getConfigId());
        }

        // 如果没有configId但有userId，获取用户的默认模型配置
        if (request.getUserId() != null) {
            logger.info("获取用户默认模型配置，用户ID: {}", request.getUserId());
            AiModelConfig defaultConfig = configService.getDefaultConfig(request.getUserId());
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
     * 根据配置获取或创建ChatClient（使用缓存）
     * @param config 模型配置
     * @return ChatClient实例
     */
    private ChatClient getOrCreateClient(AiModelConfig config) {
        return clientCache.computeIfAbsent(config.getId(), id -> {
            logger.info("创建新的ChatClient实例，配置ID: {}", id);
            return createChatClient(config);
        });
    }

    /**
     * 根据配置创建ChatClient（使用Spring AI 1.1 Builder模式）
     * 完全使用用户配置的URL，不做任何自动拼接
     * @param config 模型配置
     * @return ChatClient实例
     */
    private ChatClient createChatClient(AiModelConfig config) {
        // 从用户配置的URL中提取baseUrl和path
        String[] urlParts = splitUrl(config.getApiEndpoint());
        String baseUrl = urlParts[0];  // 域名部分
        String customPath = urlParts[1]; // 路径部分
        
        logger.info("用户配置URL: {}, baseUrl: {}, customPath: {}", 
                config.getApiEndpoint(), baseUrl, customPath);

        // 创建OpenAI API实例
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(config.getApiKey());

        // 设置用户配置的路径（如果有）
        if (customPath != null && !customPath.isEmpty()) {
            apiBuilder.completionsPath(customPath);
        }

        OpenAiApi openAiApi = apiBuilder.build();

        // 构建ChatOptions
        BigDecimal temperature = config.getTemperature() != null ? config.getTemperature() : new BigDecimal("0.7");
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(config.getModelName())
                .temperature(temperature.doubleValue());

        if (config.getMaxTokens() != null) {
            optionsBuilder.maxTokens(config.getMaxTokens());
        }
        if (config.getTopP() != null) {
            optionsBuilder.topP(config.getTopP().doubleValue());
        }
        if (config.getFrequencyPenalty() != null) {
            optionsBuilder.frequencyPenalty(config.getFrequencyPenalty().doubleValue());
        }
        if (config.getPresencePenalty() != null) {
            optionsBuilder.presencePenalty(config.getPresencePenalty().doubleValue());
        }

        OpenAiChatOptions options = optionsBuilder.build();

        // 创建ChatModel
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel).build();
    }

    /**
     * 将用户配置的URL拆分为baseUrl和path
     * 完全按照用户配置使用，不做任何自动拼接
     * 
     * @param fullUrl 用户配置的完整URL
     * @return 数组：[baseUrl, path]
     *         baseUrl - 域名部分，如 "https://dashscope.aliyuncs.com"
     *         path - 路径部分，如 "/compatible-mode/v1/chat/completions"，如果没有路径则为null
     */
    private String[] splitUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return new String[]{fullUrl, null};
        }

        // 去除末尾斜杠
        String url = fullUrl.endsWith("/") ? fullUrl.substring(0, fullUrl.length() - 1) : fullUrl;

        try {
            // 查找协议结束位置
            int schemeEnd = url.indexOf("://");
            if (schemeEnd <= 0) {
                // 没有协议，直接返回
                return new String[]{url, null};
            }

            // 提取协议后的部分
            String afterScheme = url.substring(schemeEnd + 3);
            
            // 查找第一个斜杠（路径开始位置）
            int pathStart = afterScheme.indexOf("/");
            
            if (pathStart <= 0) {
                // 没有路径部分，只有域名
                // 例如：https://api.deepseek.com
                return new String[]{url, null};
            }

            // 有路径部分，拆分域名和路径
            // 例如：https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
            // baseUrl: https://dashscope.aliyuncs.com
            // path: /compatible-mode/v1/chat/completions
            String domain = url.substring(0, schemeEnd + 3 + pathStart);
            String path = afterScheme.substring(pathStart);
            
            return new String[]{domain, path};
            
        } catch (Exception e) {
            logger.warn("URL拆分失败: {}, 错误: {}", fullUrl, e.getMessage());
            return new String[]{fullUrl, null};
        }
    }

    /**
     * 清除指定配置的缓存（配置更新时调用）
     * @param configId 配置ID
     */
    public void clearCache(Long configId) {
        clientCache.remove(configId);
        logger.info("清除ChatClient缓存，配置ID: {}", configId);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        clientCache.clear();
        logger.info("清除所有ChatClient缓存");
    }
}