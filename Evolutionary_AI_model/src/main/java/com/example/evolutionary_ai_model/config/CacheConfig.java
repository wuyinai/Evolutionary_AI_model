package com.example.evolutionary_ai_model.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 用法：缓存配置类，配置二级缓存架构（本地缓存+Redis二级缓存）。
 * 位于配置层，负责依赖注入配置和缓存管理器注册。
 * 使用Caffeine作为一级本地缓存，Redis作为二级分布式缓存。
 * 采用注册表模式，统一管理缓存管理器实例。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 一级缓存管理器（Caffeine本地缓存）
     * 用于缓存频繁访问的聊天消息，减少Redis访问压力
     * @return Caffeine缓存管理器
     */
    @Bean("localCacheManager")
    @Primary
    public CacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(100)
                // 最大容量
                .maximumSize(1000)
                // 写入后过期时间（5分钟）
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 访问后过期时间（10分钟）
                .expireAfterAccess(10, TimeUnit.MINUTES)
                // 启用统计
                .recordStats());
        return cacheManager;
    }

    /**
     * 二级缓存管理器（Redis分布式缓存）
     * 用于缓存需要持久化和跨实例共享的数据
     * @param redisConnectionFactory Redis连接工厂
     * @return Redis缓存管理器
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 配置Redis缓存序列化和过期时间
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存过期时间（30分钟）
                .entryTtl(Duration.ofMinutes(30))
                // 禁用空值缓存
                .disableCachingNullValues()
                // 设置Key序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置Value序列化器（使用JSON序列化）
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                // 启用事务支持
                .transactionAware()
                .build();
    }
}