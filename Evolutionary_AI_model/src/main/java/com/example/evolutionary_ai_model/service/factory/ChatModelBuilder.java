package com.example.evolutionary_ai_model.service.factory;

import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import org.springframework.ai.chat.model.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用法：ChatModel构建器接口，定义不同协议的ChatModel构建策略。
 * 采用策略模式，每个协议实现一个构建器，由ProviderChatModelFactory路由调用。
 */
public interface ChatModelBuilder {

    /**
     * 获取支持的协议类型
     * @return 协议枚举
     */
    ModelProtocol getSupportedProtocol();

    /**
     * 构建ChatModel实例
     * @param apiKey API密钥（已解密）
     * @param baseUrl API基础URL
     * @param modelName 模型名称
     * @param temperature 温度参数
     * @param maxTokens 最大Token数
     * @param extraConfig 扩展配置JSON
     * @return ChatModel实例
     */
    ChatModel build(String apiKey, String baseUrl, String modelName, 
                    Double temperature, Integer maxTokens, String extraConfig);
}