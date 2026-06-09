package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.AiConversation;

/**
 * 用法：AI会话服务接口，定义会话相关的业务操作。
 * 位于业务逻辑层，负责会话的创建、更新、钉选模型等业务逻辑。
 */
public interface AiConversationService {

    /**
     * 创建新会话
     * @param userId 用户ID
     * @param title 会话标题
     * @param configId 模型配置ID
     * @return 会话实体
     */
    AiConversation createConversation(Long userId, String title, Long configId);

    /**
     * 创建或更新会话（用于聊天消息保存时）
     * 如果会话ID不存在，则创建新会话；如果会话ID存在，则更新会话的统计信息
     * @param conversationId 会话ID（可为null，如果为null则创建新会话）
     * @param userId 用户ID
     * @param title 会话标题（第一条用户消息）
     * @param configId 模型配置ID
     * @return 会话实体
     */
    AiConversation createOrUpdateConversation(String conversationId, Long userId, String title, Long configId);

    /**
     * 获取会话详情
     * @param conversationId 会话ID
     * @return 会话实体
     */
    AiConversation getConversation(String conversationId);

    /**
     * 钉选模型到会话
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param modelConfigId 模型配置ID
     */
    void pinModelToConversation(String conversationId, Long userId, Long modelConfigId);

    /**
     * 获取会话钉选的模型配置ID
     * @param conversationId 会话ID
     * @return 钉选的模型配置ID，如果没有钉选则返回null
     */
    Long getPinnedModelConfigId(String conversationId);

    /**
     * 更新会话标题
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param title 新标题
     */
    void updateTitle(String conversationId, Long userId, String title);

    /**
     * 更新会话统计信息
     * @param conversationId 会话ID
     * @param tokens 本次对话使用的Token数
     * @param cost 本次对话的费用
     */
    void updateStatistics(String conversationId, Long tokens, java.math.BigDecimal cost);
}