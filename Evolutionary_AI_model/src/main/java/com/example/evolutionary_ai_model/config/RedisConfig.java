package com.example.evolutionary_ai_model.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 用法：Redis配置类，配置Redis模板和序列化器。
 * 位于配置层，负责依赖注入配置和Redis模板注册。
 * 采用注册表模式，统一管理Redis模板实例。
 */
@Configuration
public class RedisConfig {

    /**
     * Redis模板配置
     * 使用String序列化器作为Key序列化器，JSON序列化器作为Value序列化器
     * @param redisConnectionFactory Redis连接工厂
     * @return Redis模板实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置Key序列化器
        template.setKeySerializer(new StringRedisSerializer());

        // 设置Value序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 设置Hash Key序列化器
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置Hash Value序列化器
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 设置默认序列化器
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}