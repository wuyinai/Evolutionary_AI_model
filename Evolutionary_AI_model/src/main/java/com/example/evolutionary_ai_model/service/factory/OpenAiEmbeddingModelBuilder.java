package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.stereotype.Component;


import java.util.Map;

import static org.springframework.ai.document.MetadataMode.EMBED;

/**
 * 用法：OpenAI协议的EmbeddingModel构建器，支持OpenAI、DeepSeek、通义千问等兼容OpenAI协议的服务。
 * 位于工厂层，负责构建OpenAI协议的EmbeddingModel实例。
 */
@Component
public class OpenAiEmbeddingModelBuilder implements EmbeddingModelBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiEmbeddingModelBuilder.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelProtocol getSupportedProtocol() {
        return ModelProtocol.OPENAI;
    }

    @Override
    public EmbeddingModel build(String apiKey, String baseUrl, String modelName,
                                Integer dimensions, String extraConfig) {
        logger.info("构建OpenAI EmbeddingModel，模型: {}, 基础URL: {}", modelName, baseUrl);

        try {
            // 创建OpenAI API实例（使用builder模式）
            OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                    .apiKey(apiKey);
            
            // 设置baseUrl（如果指定）
            if (baseUrl != null && !baseUrl.isEmpty()) {
                apiBuilder.baseUrl(baseUrl);
            }

            OpenAiApi openAiApi = apiBuilder.build();

            // 构建EmbeddingOptions
            OpenAiEmbeddingOptions.Builder optionsBuilder = OpenAiEmbeddingOptions.builder()
                    .model(modelName);

            // 设置向量维度（如果指定）
            if (dimensions != null && dimensions > 0) {
                optionsBuilder.dimensions(dimensions);
            }

            // 解析扩展配置
            if (extraConfig != null && !extraConfig.isEmpty()) {
                try {
                    Map<String, Object> configMap = objectMapper.readValue(extraConfig, Map.class);

                    // 处理其他可能的配置项
                    if (configMap.containsKey("dimensions")) {
                        Object dimValue = configMap.get("dimensions");
                        if (dimValue instanceof Integer) {
                            optionsBuilder.dimensions((Integer) dimValue);
                        } else if (dimValue instanceof Number) {
                            optionsBuilder.dimensions(((Number) dimValue).intValue());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析扩展配置失败: {}", e.getMessage());
                }
            }

            OpenAiEmbeddingOptions options = optionsBuilder.build();

            // 创建EmbeddingModel（使用构造函数）
            OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(
                    openAiApi,
                    EMBED,
                    options,
                    RetryUtils.DEFAULT_RETRY_TEMPLATE
            );

            logger.info("OpenAI EmbeddingModel构建完成");
            return embeddingModel;

        } catch (Exception e) {
            logger.error("构建OpenAI EmbeddingModel失败", e);
            throw new RuntimeException("构建EmbeddingModel失败: " + e.getMessage(), e);
        }
    }
}
