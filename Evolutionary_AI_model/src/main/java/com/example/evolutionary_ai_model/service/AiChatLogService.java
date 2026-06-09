package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.AiChatLog;
import com.example.evolutionary_ai_model.entity.AiConversationMessage;
import com.example.evolutionary_ai_model.entity.vo.ConversationMessageVO;

import java.util.List;

/**
 * 用法：AI聊天日志服务接口，定义聊天日志和消息存储相关的业务操作。
 * 位于业务逻辑层，负责聊天日志的保存、查询、缓存管理等业务逻辑。
 */
public interface AiChatLogService {

    /**
     * 保存聊天日志（异步）
     * @param chatLog 聊天日志实体
     */
    void saveChatLogAsync(AiChatLog chatLog);

    /**
     * 保存会话消息（异步）
     * @param message 会话消息实体
     */
    void saveConversationMessageAsync(AiConversationMessage message);

    /**
     * 批量保存会话消息（异步）
     * @param messages 会话消息列表
     */
    void saveConversationMessagesAsync(List<AiConversationMessage> messages);

    /**
     * 从缓存获取会话消息列表
     * @param conversationId 会话ID
     * @return 会话消息列表
     */
    List<AiConversationMessage> getConversationMessagesFromCache(String conversationId);

    /**
     * 将会话消息列表缓存到本地缓存和Redis
     * @param conversationId 会话ID
     * @param messages 会话消息列表
     */
    void cacheConversationMessages(String conversationId, List<AiConversationMessage> messages);

    /**
     * 清除会话消息缓存
     * @param conversationId 会话ID
     */
    void clearConversationMessagesCache(String conversationId);

    /**
     * 获取聊天日志详情
     * @param logId 日志ID
     * @return 聊天日志实体
     */
    AiChatLog getChatLogById(Long logId);

    /**
     * 获取会话的所有聊天日志
     * @param conversationId 会话ID
     * @return 聊天日志列表
     */
    List<AiChatLog> getChatLogsByConversationId(String conversationId);

    /**
     * 获取会话的所有消息（优先从缓存读取）
     * @param conversationId 会话ID
     * @return 会话消息VO列表
     */
    List<ConversationMessageVO> getConversationMessages(String conversationId);

    /**
     * 获取用户的所有会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<com.example.evolutionary_ai_model.entity.AiConversation> getUserConversations(Long userId);
}