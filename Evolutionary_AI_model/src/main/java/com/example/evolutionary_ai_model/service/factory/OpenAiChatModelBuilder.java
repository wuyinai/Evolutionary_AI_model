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
 * 用法：OpenAI协议ChatModel构建器，构建兼容OpenAI API格式的ChatModel实例。
 * 支持OpenAI、DeepSeek、通义千问等使用OpenAI兼容协议的供应商。
 * 位于工厂层，实现ChatModelBuilder接口，由ProviderChatModelFactory调用。
 */
@Component
public class OpenAiChatModelBuilder implements ChatModelBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiChatModelBuilder.class);

    @Override
    public ModelProtocol getSupportedProtocol() {
        return ModelProtocol.OPENAI;
    }

    @Override
    public ChatModel build(String apiKey, String baseUrl, String modelName,
                          Double temperature, Integer maxTokens, String extraConfig) {
        logger.info("构建OpenAI协议ChatModel，baseUrl: {}, model: {}", baseUrl, modelName);

        // 创建OpenAI API实例
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey);

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
                .openAiApi(apiBuilder.build())
                .defaultOptions(options)
                .build();

        logger.info("OpenAI协议ChatModel构建完成");
        return chatModel;
    }
}