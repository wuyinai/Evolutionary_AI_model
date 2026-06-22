package com.example.evolutionary_ai_model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

        // 创建支持Java 8日期时间类型的ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册JavaTimeModule以支持LocalDateTime等Java 8日期时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳的特性，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略null值
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 创建配置了JavaTimeModule的JSON序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 设置Key序列化器
        template.setKeySerializer(new StringRedisSerializer());

        // 设置Value序列化器
        template.setValueSerializer(jsonSerializer);

        // 设置Hash Key序列化器
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置Hash Value序列化器
        template.setHashValueSerializer(jsonSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}