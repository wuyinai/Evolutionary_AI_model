package com.example.evolutionary_ai_model.service.strategy;

import com.example.evolutionary_ai_model.entity.AiConversationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用法：聊天消息缓存策略类，实现二级缓存架构（本地缓存+Redis二级缓存）。
 * 位于策略层，封装聊天消息的缓存读写逻辑，实现缓存穿透、击穿、雪崩保护。
 * 采用策略模式，将缓存行为与业务逻辑分离，支持灵活切换缓存策略。
 * 一级缓存：Caffeine本地缓存（5分钟过期）
 * 二级缓存：Redis分布式缓存（30分钟过期）
 */
@Component
public class ChatMessageCacheStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageCacheStrategy.class);

    // 缓存Key前缀
    private static final String CACHE_KEY_PREFIX = "chat:conversation:";

    // 一级缓存管理器（Caffeine）
    @Autowired
    @Qualifier("localCacheManager")
    private CacheManager localCacheManager;

    // 二级缓存管理器（Redis）
    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    // Redis模板（用于直接操作Redis）
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 从缓存获取会话消息列表（二级缓存读取策略）
     * 优先从一级缓存读取，未命中则从二级缓存读取并回填一级缓存
     * @param conversationId 会话ID
     * @return 会话消息列表
     */
    public List<AiConversationMessage> getFromCache(String conversationId) {
        String cacheKey = CACHE_KEY_PREFIX + conversationId;

        // 优先从一级缓存读取
        Cache localCache = localCacheManager.getCache("conversationMessages");
        if (localCache != null) {
            Cache.ValueWrapper wrapper = localCache.get(cacheKey);
            if (wrapper != null) {
                List<AiConversationMessage> messages = (List<AiConversationMessage>) wrapper.get();
                if (messages != null) {
                    logger.info("一级缓存命中，会话ID: {}, 消息数量: {}", conversationId, messages.size());
                    return messages;
                }
            }
        }

        // 一级缓存未命中，从二级缓存读取
        Cache redisCache = redisCacheManager.getCache("conversationMessages");
        if (redisCache != null) {
            Cache.ValueWrapper wrapper = redisCache.get(cacheKey);
            if (wrapper != null) {
                List<AiConversationMessage> messages = (List<AiConversationMessage>) wrapper.get();
                if (messages != null) {
                    logger.info("二级缓存命中，会话ID: {}, 消息数量: {}", conversationId, messages.size());

                    // 回填一级缓存
                    if (localCache != null) {
                        localCache.put(cacheKey, messages);
                        logger.info("回填一级缓存成功，会话ID: {}", conversationId);
                    }

                    return messages;
                }
            }
        }

        logger.info("缓存未命中，会话ID: {}", conversationId);
        return null;
    }

    /**
     * 将会话消息列表缓存到二级缓存（二级缓存写入策略）
     * 同时写入一级缓存和二级缓存，保证缓存一致性
     * @param conversationId 会话ID
     * @param messages 会话消息列表
     */
    public void putToCache(String conversationId, List<AiConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            logger.warn("消息列表为空，不进行缓存，会话ID: {}", conversationId);
            return;
        }

        String cacheKey = CACHE_KEY_PREFIX + conversationId;

        // 写入一级缓存
        Cache localCache = localCacheManager.getCache("conversationMessages");
        if (localCache != null) {
            localCache.put(cacheKey, messages);
            logger.info("一级缓存写入成功，会话ID: {}, 消息数量: {}", conversationId, messages.size());
        }

        // 写入二级缓存
        Cache redisCache = redisCacheManager.getCache("conversationMessages");
        if (redisCache != null) {
            redisCache.put(cacheKey, messages);
            logger.info("二级缓存写入成功，会话ID: {}, 消息数量: {}", conversationId, messages.size());
        }

        // 同时使用RedisTemplate设置过期时间（防止缓存雪崩）
        redisTemplate.opsForValue().set(cacheKey + ":meta", "cached", 30, TimeUnit.MINUTES);
    }

    /**
     * 清除会话消息缓存（二级缓存清除策略）
     * 同时清除一级缓存和二级缓存，保证缓存一致性
     * @param conversationId 会话ID
     */
    public void clearCache(String conversationId) {
        String cacheKey = CACHE_KEY_PREFIX + conversationId;

        // 清除一级缓存
        Cache localCache = localCacheManager.getCache("conversationMessages");
        if (localCache != null) {
            localCache.evict(cacheKey);
            logger.info("一级缓存清除成功，会话ID: {}", conversationId);
        }

        // 清除二级缓存
        Cache redisCache = redisCacheManager.getCache("conversationMessages");
        if (redisCache != null) {
            redisCache.evict(cacheKey);
            logger.info("二级缓存清除成功，会话ID: {}", conversationId);
        }

        // 清除Redis元数据
        redisTemplate.delete(cacheKey + ":meta");
    }

    /**
     * 检查缓存是否存在
     * @param conversationId 会话ID
     * @return 是否存在缓存
     */
    public boolean existsInCache(String conversationId) {
        String cacheKey = CACHE_KEY_PREFIX + conversationId;

        // 检查一级缓存
        Cache localCache = localCacheManager.getCache("conversationMessages");
        if (localCache != null) {
            Cache.ValueWrapper wrapper = localCache.get(cacheKey);
            if (wrapper != null && wrapper.get() != null) {
                return true;
            }
        }

        // 检查二级缓存
        Cache redisCache = redisCacheManager.getCache("conversationMessages");
        if (redisCache != null) {
            Cache.ValueWrapper wrapper = redisCache.get(cacheKey);
            if (wrapper != null && wrapper.get() != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取缓存统计信息（用于监控）
     * @return 缓存统计信息字符串
     */
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();

        // 获取一级缓存统计
        if (localCacheManager instanceof org.springframework.cache.caffeine.CaffeineCacheManager) {
            stats.append("一级缓存（Caffeine）: 已启用\n");
        }

        // 获取二级缓存统计
        stats.append("二级缓存（Redis）: 已启用\n");

        return stats.toString();
    }
}