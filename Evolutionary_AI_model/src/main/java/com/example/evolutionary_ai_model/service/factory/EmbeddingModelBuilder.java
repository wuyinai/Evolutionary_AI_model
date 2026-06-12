package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * 用法：EmbeddingModel构建器接口，定义不同协议的EmbeddingModel构建策略。
 * 采用策略模式，每个协议实现一个构建器，由ProviderEmbeddingModelFactory路由调用。
 */
public interface EmbeddingModelBuilder {

    /**
     * 获取支持的协议类型
     * @return 协议枚举
     */
    ModelProtocol getSupportedProtocol();

    /**
     * 构建EmbeddingModel实例
     * @param apiKey API密钥（已解密）
     * @param baseUrl API基础URL
     * @param modelName 模型名称
     * @param dimensions 向量维度（可选）
     * @param extraConfig 扩展配置JSON
     * @return EmbeddingModel实例
     */
    EmbeddingModel build(String apiKey, String baseUrl, String modelName,
                        Integer dimensions, String extraConfig);
}
