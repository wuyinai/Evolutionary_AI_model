package com.example.evolutionary_ai_model.service.strategy;

import cn.hutool.core.util.IdUtil;
import com.example.evolutionary_ai_model.entity.*;
import com.example.evolutionary_ai_model.entity.dto.ChatMessageDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.service.AiChatLogService;
import com.example.evolutionary_ai_model.service.AiConversationService;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.service.RagService;
import com.example.evolutionary_ai_model.service.factory.ProviderChatModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 用法：动态模型对话服务类，根据两级配置和会话钉选动态创建ChatClient进行对话。
 * 支持会话级模型钉选，优先使用会话钉选的模型配置，确保"用户选什么用什么"。
 * 使用ProviderChatModelFactory根据协议类型动态构建ChatModel实例。
 * 位于策略层，封装动态模型选择和聊天消息存储逻辑，实现聊天记录持久化和二级缓存。
 */
@Component
public class DynamicChatStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DynamicChatStrategy.class);

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

    // 聊天日志服务
    @Autowired
    private AiChatLogService chatLogService;

    // RAG服务
    @Autowired
    private RagService ragService;

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

            // 构建基础提示词
            String basePrompt = buildPrompt(request.getMessage(), request.getHistory());

            // 最终提示词（可能经过RAG增强）
            String prompt;

            // 如果指定了知识库文档ID列表，进行RAG检索增强
            if (request.getKnowledgeDocumentIds() != null && !request.getKnowledgeDocumentIds().isEmpty()) {
                logger.info("开始RAG检索增强，知识库文档数量: {}", request.getKnowledgeDocumentIds().size());

                // 检索相关内容
                int topK = request.getRagTopK() != null ? request.getRagTopK() : 3;
                List<String> relevantContent = ragService.retrieveRelevantContent(
                        request.getKnowledgeDocumentIds(), request.getMessage(), topK);

                // 构建RAG增强提示词
                prompt = ragService.buildRagPrompt(basePrompt, relevantContent);
                logger.info("RAG增强提示词构建完成，相关内容数量: {}", relevantContent.size());
            } else {
                prompt = basePrompt;
            }

            // 记录请求开始时间
            LocalDateTime requestTime = LocalDateTime.now();
            long startTime = System.currentTimeMillis();

            // 使用AtomicReference收集响应内容
            AtomicReference<StringBuilder> responseBuilder = new AtomicReference<>(new StringBuilder());

            // 流式调用AI模型，并在流式响应过程中收集内容
            return chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content()
                    // 收集每个响应片段
                    .doOnNext(chunk -> {
                        responseBuilder.get().append(chunk);
                        logger.debug("收到响应片段，长度: {}", chunk.length());
                    })
                    // 流式响应完成后，保存聊天日志和消息
                    .doOnComplete(() -> {
                        String responseContent = responseBuilder.get().toString();
                        long endTime = System.currentTimeMillis();
                        long latencyMs = endTime - startTime;

                        logger.info("流式响应完成，总长度: {}, 耗时: {}ms", responseContent.length(), latencyMs);

                        // 异步保存聊天日志和会话消息
                        saveChatLogAndMessageAsync(request, modelConfig, providerConfig, 
                                prompt, responseContent, requestTime, latencyMs);
                    })
                    // 流式响应出错时，记录错误日志
                    .doOnError(error -> {
                        logger.error("流式响应出错，会话ID: {}", request.getConversationId(), error);

                        // 异步保存错误日志
                        saveErrorLogAsync(request, modelConfig, providerConfig, 
                                prompt, error.getMessage(), requestTime);
                    });

        } catch (Exception e) {
            logger.error("动态模式流式对话异常", e);
            return Flux.error(new RuntimeException("AI流式对话失败: " + e.getMessage()));
        }
    }

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

    /**
     * 异步保存聊天日志和会话消息
     * @param request 对话请求
     * @param modelConfig 模型配置
     * @param providerConfig 供应商配置
     * @param requestContent 请求内容
     * @param responseContent 响应内容
     * @param requestTime 请求时间
     * @param latencyMs 响应耗时
     */
    private void saveChatLogAndMessageAsync(ChatRequestDTO request, AiModelConfig modelConfig, 
            AiProviderConfig providerConfig, String requestContent, String responseContent, 
            LocalDateTime requestTime, long latencyMs) {
        try {
            // 创建或更新会话记录（确保会话表有数据）
            String conversationId = request.getConversationId();
            String title = request.getMessage().length() > 50 ? 
                    request.getMessage().substring(0, 50) + "..." : request.getMessage();
            
            AiConversation conversation = conversationService.createOrUpdateConversation(
                    conversationId, request.getUserId(), title, modelConfig.getId());
            
            // 更新conversationId（如果之前为null，现在有了新的conversationId）
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = conversation.getConversationId();
                request.setConversationId(conversationId);
                logger.info("新创建的会话ID: {}", conversationId);
            }

            // 创建聊天日志
            AiChatLog chatLog = new AiChatLog();
            chatLog.setTraceId(IdUtil.fastSimpleUUID());
            chatLog.setConfigId(modelConfig.getId());
            chatLog.setProviderCode(providerConfig.getProviderCode());
            chatLog.setModelName(modelConfig.getModelName());
            chatLog.setUserId(request.getUserId());
            chatLog.setConversationId(conversationId);
            chatLog.setRequestType("CHAT");
            chatLog.setRequestContent(requestContent);
            chatLog.setResponseContent(responseContent);
            chatLog.setIsStreaming(1);
            chatLog.setResponseStatus("SUCCESS");
            chatLog.setLatencyMs(latencyMs);
            chatLog.setRequestTime(requestTime);
            chatLog.setResponseTime(LocalDateTime.now());

            // 异步保存聊天日志
            chatLogService.saveChatLogAsync(chatLog);

            // 创建用户消息
            AiConversationMessage userMessage = new AiConversationMessage();
            userMessage.setConversationId(conversationId);
            userMessage.setRole("USER");
            userMessage.setContent(request.getMessage());

            // 创建助手消息
            AiConversationMessage assistantMessage = new AiConversationMessage();
            assistantMessage.setConversationId(conversationId);
            assistantMessage.setRole("ASSISTANT");
            assistantMessage.setContent(responseContent);
            assistantMessage.setLogId(chatLog.getId());

            // 异步保存会话消息
            List<AiConversationMessage> messages = new ArrayList<>();
            messages.add(userMessage);
            messages.add(assistantMessage);
            chatLogService.saveConversationMessagesAsync(messages);

            // 更新会话统计信息
            // 简化Token计算（按字符数估算）
            int estimatedTokens = (requestContent.length() + responseContent.length()) / 4;
            conversationService.updateStatistics(conversationId, 
                    (long) estimatedTokens, BigDecimal.ZERO);

            logger.info("聊天日志和消息异步保存任务已提交，会话ID: {}", conversationId);

        } catch (Exception e) {
            logger.error("异步保存聊天日志和消息失败", e);
        }
    }

    /**
     * 异步保存错误日志
     * @param request 对话请求
     * @param modelConfig 模型配置
     * @param providerConfig 供应商配置
     * @param requestContent 请求内容
     * @param errorMessage 错误信息
     * @param requestTime 请求时间
     */
    private void saveErrorLogAsync(ChatRequestDTO request, AiModelConfig modelConfig, 
            AiProviderConfig providerConfig, String requestContent, String errorMessage, 
            LocalDateTime requestTime) {
        try {
            // 创建错误日志
            AiChatLog errorLog = new AiChatLog();
            errorLog.setTraceId(IdUtil.fastSimpleUUID());
            errorLog.setConfigId(modelConfig.getId());
            errorLog.setProviderCode(providerConfig.getProviderCode());
            errorLog.setModelName(modelConfig.getModelName());
            errorLog.setUserId(request.getUserId());
            errorLog.setConversationId(request.getConversationId());
            errorLog.setRequestType("CHAT");
            errorLog.setRequestContent(requestContent);
            errorLog.setIsStreaming(1);
            errorLog.setResponseStatus("FAILED");
            errorLog.setErrorMessage(errorMessage);
            errorLog.setRequestTime(requestTime);
            errorLog.setResponseTime(LocalDateTime.now());

            // 异步保存错误日志
            chatLogService.saveChatLogAsync(errorLog);

            logger.info("错误日志异步保存任务已提交");

        } catch (Exception e) {
            logger.error("异步保存错误日志失败", e);
        }
    }
}