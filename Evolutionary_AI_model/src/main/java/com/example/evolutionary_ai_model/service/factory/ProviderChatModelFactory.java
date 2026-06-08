package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用法：Provider ChatModel工厂，根据协议类型路由到对应的ChatModelBuilder构建ChatModel实例。
 * 采用工厂模式，通过协议枚举驱动，支持OpenAI、Ollama等多种协议。
 * 位于工厂层，协调协议构建器和配置信息，动态创建ChatClient实例。
 */
@Component
public class ProviderChatModelFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProviderChatModelFactory.class);

    // 协议构建器注册表
    private final Map<ModelProtocol, ChatModelBuilder> builderMap = new HashMap<>();

    // ChatClient缓存，避免重复创建
    private final Map<Long, ChatClient> clientCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 构造函数，通过Spring依赖注入自动注册所有构建器
     * @param builders 所有ChatModelBuilder实现类的列表
     */
    public ProviderChatModelFactory(List<ChatModelBuilder> builders) {
        // 自动注册所有构建器
        for (ChatModelBuilder builder : builders) {
            registerBuilder(builder);
        }
        logger.info("已注册 {} 个协议构建器", builderMap.size());
    }

    /**
     * 注册协议构建器
     * @param builder 协议构建器实例
     */
    public void registerBuilder(ChatModelBuilder builder) {
        ModelProtocol protocol = builder.getSupportedProtocol();
        builderMap.put(protocol, builder);
        logger.info("注册协议构建器: {}", protocol.getName());
    }

    /**
     * 根据供应商配置和模型配置创建ChatClient
     * @param providerConfig 供应商配置（连接信息）
     * @param modelConfig 模型配置（推理参数）
     * @return ChatClient实例
     */
    public ChatClient createChatClient(AiProviderConfig providerConfig, AiModelConfig modelConfig) {
        logger.info("创建ChatClient，供应商配置ID: {}, 模型配置ID: {}", 
                providerConfig.getId(), modelConfig.getId());

        // 获取协议类型
        ModelProtocol protocol = ModelProtocol.fromCode(providerConfig.getProtocolType());
        if (protocol == null) {
            logger.warn("未知的协议类型: {}, 使用默认OpenAI协议", providerConfig.getProtocolType());
            protocol = ModelProtocol.OPENAI;
        }

        // 获取对应的构建器
        ChatModelBuilder builder = builderMap.get(protocol);
        if (builder == null) {
            logger.warn("未找到协议构建器: {}, 使用默认OpenAI构建器", protocol.getName());
            builder = builderMap.get(ModelProtocol.OPENAI);
            if (builder == null) {
                throw new RuntimeException("未找到可用的协议构建器");
            }
        }

        // 构建ChatModel
        ChatModel chatModel = builder.build(
                providerConfig.getApiKey(), // API密钥（已解密）
                providerConfig.getApiEndpoint(), // API端点
                modelConfig.getModelName(), // 模型名称
                modelConfig.getTemperature() != null ? modelConfig.getTemperature().doubleValue() : 0.7,
                modelConfig.getMaxTokens(),
                providerConfig.getExtraConfig()
        );

        // 创建ChatClient
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        logger.info("ChatClient创建完成，协议: {}", protocol.getName());
        return chatClient;
    }

    /**
     * 获取或创建ChatClient（使用缓存）
     * @param providerConfig 供应商配置
     * @param modelConfig 模型配置
     * @return ChatClient实例
     */
    public ChatClient getOrCreateChatClient(AiProviderConfig providerConfig, AiModelConfig modelConfig) {
        // 使用模型配置ID作为缓存key（因为推理参数可能变化）
        return clientCache.computeIfAbsent(modelConfig.getId(), id -> {
            logger.info("创建新的ChatClient实例，模型配置ID: {}", id);
            return createChatClient(providerConfig, modelConfig);
        });
    }

    /**
     * 清除指定配置的缓存
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

    /**
     * 检查协议是否支持
     * @param protocol 协议枚举
     * @return 是否支持
     */
    public boolean isProtocolSupported(ModelProtocol protocol) {
        return builderMap.containsKey(protocol);
    }

    /**
     * 获取所有支持的协议列表
     * @return 支持的协议列表
     */
    public List<ModelProtocol> getSupportedProtocols() {
        return List.copyOf(builderMap.keySet());
    }
}