package com.example.evolutionary_ai_model.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.entity.AiChatLog;
import com.example.evolutionary_ai_model.entity.AiConversation;
import com.example.evolutionary_ai_model.entity.AiConversationMessage;
import com.example.evolutionary_ai_model.entity.vo.ConversationMessageVO;
import com.example.evolutionary_ai_model.mapper.AiChatLogMapper;
import com.example.evolutionary_ai_model.mapper.AiConversationMapper;
import com.example.evolutionary_ai_model.mapper.AiConversationMessageMapper;
import com.example.evolutionary_ai_model.service.AiChatLogService;
import com.example.evolutionary_ai_model.service.strategy.ChatMessageCacheStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用法：AI聊天日志服务实现类，负责处理聊天日志和消息存储相关的业务逻辑。
 * 依赖AiChatLogMapper和AiConversationMessageMapper进行数据持久化。
 * 使用ChatMessageCacheStrategy实现二级缓存管理。
 * 位于业务逻辑层，采用异步方式保存聊天记录，避免阻塞主流程。
 */
@Service
public class AiChatLogServiceImpl implements AiChatLogService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatLogServiceImpl.class);

    @Autowired
    private AiChatLogMapper chatLogMapper;

    @Autowired
    private AiConversationMessageMapper conversationMessageMapper;

    @Autowired
    private AiConversationMapper conversationMapper;

    @Autowired
    private ChatMessageCacheStrategy cacheStrategy;

    @Override
    @Async("chatLogTaskExecutor")
    public void saveChatLogAsync(AiChatLog chatLog) {
        logger.info("异步保存聊天日志，会话ID: {}, 用户ID: {}", chatLog.getConversationId(), chatLog.getUserId());

        try {
            // 设置主键ID
            if (chatLog.getId() == null) {
                chatLog.setId(IdUtil.getSnowflakeNextId());
            }

            chatLogMapper.insert(chatLog);
            logger.info("聊天日志保存成功，日志ID: {}", chatLog.getId());

        } catch (Exception e) {
            logger.error("聊天日志保存失败，会话ID: {}", chatLog.getConversationId(), e);
        }
    }

    @Override
    @Async("chatLogTaskExecutor")
    public void saveConversationMessageAsync(AiConversationMessage message) {
        logger.info("异步保存会话消息，会话ID: {}, 角色: {}", message.getConversationId(), message.getRole());

        try {
            // 设置主键ID和消息ID
            if (message.getId() == null) {
                message.setId(IdUtil.getSnowflakeNextId());
            }
            if (message.getMessageId() == null) {
                message.setMessageId(IdUtil.fastSimpleUUID());
            }

            conversationMessageMapper.insert(message);
            logger.info("会话消息保存成功，消息ID: {}", message.getMessageId());

        } catch (Exception e) {
            logger.error("会话消息保存失败，会话ID: {}", message.getConversationId(), e);
        }
    }

    @Override
    @Async("chatLogTaskExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void saveConversationMessagesAsync(List<AiConversationMessage> messages) {
        logger.info("批量异步保存会话消息，消息数量: {}", messages.size());

        try {
            for (AiConversationMessage message : messages) {
                // 设置主键ID和消息ID
                if (message.getId() == null) {
                    message.setId(IdUtil.getSnowflakeNextId());
                }
                if (message.getMessageId() == null) {
                    message.setMessageId(IdUtil.fastSimpleUUID());
                }

                conversationMessageMapper.insert(message);
            }

            logger.info("批量会话消息保存成功，保存数量: {}", messages.size());

        } catch (Exception e) {
            logger.error("批量会话消息保存失败", e);
        }
    }

    @Override
    public List<AiConversationMessage> getConversationMessagesFromCache(String conversationId) {
        logger.info("从缓存获取会话消息，会话ID: {}", conversationId);
        return cacheStrategy.getFromCache(conversationId);
    }

    @Override
    public void cacheConversationMessages(String conversationId, List<AiConversationMessage> messages) {
        logger.info("缓存会话消息，会话ID: {}, 消息数量: {}", conversationId, messages.size());
        cacheStrategy.putToCache(conversationId, messages);
    }

    @Override
    public void clearConversationMessagesCache(String conversationId) {
        logger.info("清除会话消息缓存，会话ID: {}", conversationId);
        cacheStrategy.clearCache(conversationId);
    }

    @Override
    public AiChatLog getChatLogById(Long logId) {
        logger.info("获取聊天日志详情，日志ID: {}", logId);
        return chatLogMapper.selectById(logId);
    }

    @Override
    public List<AiChatLog> getChatLogsByConversationId(String conversationId) {
        logger.info("获取会话的所有聊天日志，会话ID: {}", conversationId);

        // 使用MyBatis-Plus的LambdaQueryWrapper查询
        LambdaQueryWrapper<AiChatLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiChatLog::getConversationId, conversationId)
                .orderByAsc(AiChatLog::getRequestTime);

        return chatLogMapper.selectList(queryWrapper);
    }

    @Override
    public List<ConversationMessageVO> getConversationMessages(String conversationId) {
        logger.info("获取会话的所有消息，会话ID: {}", conversationId);

        // 优先从缓存读取
        List<AiConversationMessage> cachedMessages = cacheStrategy.getFromCache(conversationId);
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            logger.info("从缓存获取会话消息，消息数量: {}", cachedMessages.size());
            return convertToVOList(cachedMessages);
        }

        // 缓存未命中，从数据库查询
        LambdaQueryWrapper<AiConversationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversationMessage::getConversationId, conversationId)
                .orderByAsc(AiConversationMessage::getCreateTime);

        List<AiConversationMessage> messages = conversationMessageMapper.selectList(queryWrapper);
        logger.info("从数据库获取会话消息，消息数量: {}", messages.size());

        // 将查询结果缓存
        if (messages != null && !messages.isEmpty()) {
            cacheStrategy.putToCache(conversationId, messages);
        }

        return convertToVOList(messages);
    }

    @Override
    public List<AiConversation> getUserConversations(Long userId) {
        logger.info("获取用户的所有会话列表，用户ID: {}", userId);

        LambdaQueryWrapper<AiConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getDelFlag, 0)
                .orderByDesc(AiConversation::getLastMessageTime);

        List<AiConversation> conversations = conversationMapper.selectList(queryWrapper);
        logger.info("获取用户会话列表成功，会话数量: {}", conversations.size());

        return conversations;
    }

    /**
     * 将会话消息实体列表转换为VO列表
     * @param messages 会话消息实体列表
     * @return VO列表
     */
    private List<ConversationMessageVO> convertToVOList(List<AiConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        return messages.stream().map(message -> {
            ConversationMessageVO vo = new ConversationMessageVO();
            BeanUtils.copyProperties(message, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}