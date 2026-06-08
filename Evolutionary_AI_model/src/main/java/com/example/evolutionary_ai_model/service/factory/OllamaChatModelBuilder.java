package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * 用法：Ollama协议ChatModel构建器，构建Ollama本地部署的ChatModel实例。
 * Ollama使用OpenAI兼容的API格式，但端点为本地地址。
 * 位于工厂层，实现ChatModelBuilder接口，由ProviderChatModelFactory调用。
 */
@Component
public class OllamaChatModelBuilder implements ChatModelBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OllamaChatModelBuilder.class);

    // Ollama默认端点
    private static final String DEFAULT_OLLAMA_ENDPOINT = "http://localhost:11434";

    @Override
    public ModelProtocol getSupportedProtocol() {
        return ModelProtocol.OLLAMA;
    }

    @Override
    public ChatModel build(String apiKey, String baseUrl, String modelName,
                          Double temperature, Integer maxTokens, String extraConfig) {
        logger.info("构建Ollama协议ChatModel，baseUrl: {}, model: {}", baseUrl, modelName);

        // Ollama不需要API Key，使用空字符串
        String effectiveApiKey = apiKey != null && !apiKey.isEmpty() ? apiKey : "ollama";

        // 如果没有提供baseUrl，使用默认端点
        String effectiveBaseUrl = baseUrl != null && !baseUrl.isEmpty() 
                ? baseUrl : DEFAULT_OLLAMA_ENDPOINT;

        // 创建OpenAI API实例（Ollama兼容OpenAI格式）
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(effectiveBaseUrl)
                .apiKey(effectiveApiKey)
                .build();

        // 构建ChatOptions
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(modelName);

        if (temperature != null) {
            optionsBuilder.temperature(temperature);
        }
        if (maxTokens != null) {
            optionsBuilder.maxTokens(maxTokens);
        }

        OpenAiChatOptions options = optionsBuilder.build();

        // 创建ChatModel
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        logger.info("Ollama协议ChatModel构建完成");
        return chatModel;
    }
}