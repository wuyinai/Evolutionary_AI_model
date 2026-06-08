package com.example.evolutionary_ai_model.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.evolutionary_ai_model.entity.AiConversation;
import com.example.evolutionary_ai_model.mapper.AiConversationMapper;
import com.example.evolutionary_ai_model.service.AiConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用法：AI会话服务实现类，负责处理会话相关的业务逻辑。
 * 依赖AiConversationMapper进行数据持久化，实现会话创建、模型钉选等功能。
 * 位于业务逻辑层，实现会话级模型钉选逻辑。
 */
@Service
public class AiConversationServiceImpl implements AiConversationService {

    private static final Logger logger = LoggerFactory.getLogger(AiConversationServiceImpl.class);

    @Autowired
    private AiConversationMapper conversationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConversation createConversation(Long userId, String title, Long configId) {
        logger.info("创建新会话，用户ID: {}, 标题: {}, 配置ID: {}", userId, title, configId);

        AiConversation conversation = new AiConversation();
        conversation.setId(IdUtil.getSnowflakeNextId());
        conversation.setConversationId(IdUtil.fastSimpleUUID());
        conversation.setUserId(userId);
        conversation.setConfigId(configId);
        conversation.setTitle(title);
        conversation.setMessageCount(0);
        conversation.setTotalTokens(0L);
        conversation.setTotalCost(BigDecimal.ZERO);
        conversation.setStatus(1); // 活跃状态

        conversationMapper.insert(conversation);
        logger.info("会话创建成功，会话ID: {}", conversation.getConversationId());

        return conversation;
    }

    @Override
    public AiConversation getConversation(String conversationId) {
        logger.info("获取会话详情，会话ID: {}", conversationId);

        LambdaQueryWrapper<AiConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversation::getConversationId, conversationId)
                .eq(AiConversation::getDelFlag, 0);

        AiConversation conversation = conversationMapper.selectOne(queryWrapper);
        if (conversation == null) {
            logger.warn("会话不存在，会话ID: {}", conversationId);
        }

        return conversation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinModelToConversation(String conversationId, Long userId, Long modelConfigId) {
        logger.info("钉选模型到会话，会话ID: {}, 用户ID: {}, 模型配置ID: {}", 
                conversationId, userId, modelConfigId);

        // 验证会话是否存在且属于该用户
        AiConversation conversation = getConversation(conversationId);
        if (conversation == null) {
            logger.warn("会话不存在，无法钉选模型");
            throw new IllegalArgumentException("会话不存在");
        }

        if (!conversation.getUserId().equals(userId)) {
            logger.warn("会话不属于该用户，无法钉选模型");
            throw new IllegalArgumentException("无权操作此会话");
        }

        // 更新钉选的模型配置ID
        LambdaUpdateWrapper<AiConversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiConversation::getConversationId, conversationId)
                .eq(AiConversation::getUserId, userId)
                .set(AiConversation::getPinnedConfigId, modelConfigId);

        conversationMapper.update(null, updateWrapper);
        logger.info("模型钉选成功，会话后续消息将使用模型配置ID: {}", modelConfigId);
    }

    @Override
    public Long getPinnedModelConfigId(String conversationId) {
        logger.info("获取会话钉选的模型配置ID，会话ID: {}", conversationId);

        AiConversation conversation = getConversation(conversationId);
        if (conversation == null) {
            return null;
        }

        // 返回钉选的模型配置ID，如果没有钉选则返回null
        Long pinnedConfigId = conversation.getPinnedConfigId();
        logger.info("会话钉选的模型配置ID: {}", pinnedConfigId);

        return pinnedConfigId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTitle(String conversationId, Long userId, String title) {
        logger.info("更新会话标题，会话ID: {}, 用户ID: {}, 新标题: {}", conversationId, userId, title);

        LambdaUpdateWrapper<AiConversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiConversation::getConversationId, conversationId)
                .eq(AiConversation::getUserId, userId)
                .set(AiConversation::getTitle, title);

        conversationMapper.update(null, updateWrapper);
        logger.info("会话标题更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatistics(String conversationId, Long tokens, BigDecimal cost) {
        logger.info("更新会话统计信息，会话ID: {}, Token数: {}, 费用: {}", 
                conversationId, tokens, cost);

        // 先获取当前会话信息
        AiConversation conversation = getConversation(conversationId);
        if (conversation == null) {
            logger.warn("会话不存在，无法更新统计信息");
            return;
        }

        // 更新统计信息
        LambdaUpdateWrapper<AiConversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiConversation::getConversationId, conversationId)
                .set(AiConversation::getMessageCount, conversation.getMessageCount() + 1)
                .set(AiConversation::getTotalTokens, conversation.getTotalTokens() + tokens)
                .set(AiConversation::getTotalCost, conversation.getTotalCost().add(cost))
                .set(AiConversation::getLastMessageTime, java.time.LocalDateTime.now());

        conversationMapper.update(null, updateWrapper);
        logger.info("会话统计信息更新成功");
    }
}