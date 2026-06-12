package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用法：Provider EmbeddingModel工厂，根据协议类型路由到对应的EmbeddingModelBuilder构建EmbeddingModel实例。
 * 采用工厂模式，通过协议枚举驱动，支持OpenAI、Ollama等多种协议。
 * 位于工厂层，协调协议构建器和配置信息，动态创建EmbeddingModel实例。
 */
@Component
public class ProviderEmbeddingModelFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProviderEmbeddingModelFactory.class);

    // 协议构建器注册表
    private final Map<ModelProtocol, EmbeddingModelBuilder> builderMap = new HashMap<>();

    // EmbeddingModel缓存，避免重复创建
    private final Map<Long, EmbeddingModel> modelCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 构造函数，通过Spring依赖注入自动注册所有构建器
     * @param builders 所有EmbeddingModelBuilder实现类的列表
     */
    public ProviderEmbeddingModelFactory(List<EmbeddingModelBuilder> builders) {
        // 自动注册所有构建器
        for (EmbeddingModelBuilder builder : builders) {
            registerBuilder(builder);
        }
        logger.info("已注册 {} 个Embedding协议构建器", builderMap.size());
    }

    /**
     * 注册协议构建器
     * @param builder 协议构建器实例
     */
    public void registerBuilder(EmbeddingModelBuilder builder) {
        ModelProtocol protocol = builder.getSupportedProtocol();
        builderMap.put(protocol, builder);
        logger.info("注册Embedding协议构建器: {}", protocol.getName());
    }

    /**
     * 根据供应商配置和模型配置创建EmbeddingModel
     * @param providerConfig 供应商配置（连接信息）
     * @param modelConfig 模型配置（推理参数）
     * @return EmbeddingModel实例
     */
    public EmbeddingModel createEmbeddingModel(AiProviderConfig providerConfig, AiModelConfig modelConfig) {
        logger.info("创建EmbeddingModel，供应商配置ID: {}, 模型配置ID: {}",
                providerConfig.getId(), modelConfig.getId());

        // 获取协议类型
        ModelProtocol protocol = ModelProtocol.fromCode(providerConfig.getProtocolType());
        if (protocol == null) {
            logger.warn("未知的协议类型: {}, 使用默认OpenAI协议", providerConfig.getProtocolType());
            protocol = ModelProtocol.OPENAI;
        }

        // 获取对应的构建器
        EmbeddingModelBuilder builder = builderMap.get(protocol);
        if (builder == null) {
            logger.warn("未找到Embedding协议构建器: {}, 使用默认OpenAI构建器", protocol.getName());
            builder = builderMap.get(ModelProtocol.OPENAI);
            if (builder == null) {
                throw new RuntimeException("未找到可用的Embedding协议构建器");
            }
        }

        // 构建EmbeddingModel
        EmbeddingModel embeddingModel = builder.build(
                providerConfig.getApiKey(), // API密钥（已解密）
                providerConfig.getApiEndpoint(), // API端点
                modelConfig.getModelName(), // 模型名称
                modelConfig.getVectorDimensions(), // 向量维度
                providerConfig.getExtraConfig()
        );

        logger.info("EmbeddingModel创建完成，协议: {}", protocol.getName());
        return embeddingModel;
    }

    /**
     * 获取或创建EmbeddingModel（使用缓存）
     * @param providerConfig 供应商配置
     * @param modelConfig 模型配置
     * @return EmbeddingModel实例
     */
    public EmbeddingModel getOrCreateEmbeddingModel(AiProviderConfig providerConfig, AiModelConfig modelConfig) {
        // 使用模型配置ID作为缓存key
        return modelCache.computeIfAbsent(modelConfig.getId(), id -> {
            logger.info("创建新的EmbeddingModel实例，模型配置ID: {}", id);
            return createEmbeddingModel(providerConfig, modelConfig);
        });
    }

    /**
     * 清除指定配置的缓存
     * @param configId 配置ID
     */
    public void clearCache(Long configId) {
        modelCache.remove(configId);
        logger.info("清除EmbeddingModel缓存，配置ID: {}", configId);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        modelCache.clear();
        logger.info("清除所有EmbeddingModel缓存");
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
