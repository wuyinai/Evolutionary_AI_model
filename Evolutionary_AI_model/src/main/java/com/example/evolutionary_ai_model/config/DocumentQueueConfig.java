package com.example.evolutionary_ai_model.config;

import com.example.evolutionary_ai_model.common.constant.QueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用法：文档处理队列配置类，定义文档处理相关的交换机、队列和绑定关系。
 * 位于配置层，负责RabbitMQ消息队列基础设施的声明式配置。
 * 采用企业级消息队列设计模式，支持死信队列、延迟重试、消息持久化。
 */
@Configuration
public class DocumentQueueConfig {

    /**
     * 文档处理交换机（Direct类型）
     * 特点：消息路由到RoutingKey完全匹配的队列
     */
    @Bean
    public DirectExchange documentProcessExchange() {
        return ExchangeBuilder
                .directExchange(QueueConstants.DOCUMENT_PROCESS_EXCHANGE)
                .durable(true) // 持久化，重启后交换机不丢失
                .build();
    }

    /**
     * 死信交换机（Direct类型）
     * 用于接收处理失败的消息
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 文档解析队列
     * 配置死信队列，消息过期或被拒绝后进入死信队列
     */
    @Bean
    public Queue documentParseQueue() {
        return QueueBuilder
                .durable(QueueConstants.DOCUMENT_PARSE_QUEUE)
                // 死信队列配置：消息过期后转发到死信交换机
                .deadLetterExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(QueueConstants.DOCUMENT_PARSE_DLQ_ROUTING_KEY)
                // 消息过期时间（毫秒）：10分钟
                .ttl(QueueConstants.DOCUMENT_PARSE_EXPIRATION.intValue())
                .build();
    }

    /**
     * 文档解析死信队列（DLQ）
     * 用于存储处理失败的消息，便于人工干预或重试
     */
    @Bean
    public Queue documentParseDLQ() {
        return QueueBuilder
                .durable(QueueConstants.DOCUMENT_PARSE_DLQ)
                .build();
    }

    /**
     * 文档解析延迟队列
     * 用于实现消息的延迟重试机制
     * 消息过期后会自动路由到死信交换机，再路由回原队列
     */
    @Bean
    public Queue documentParseDelayQueue() {
        return QueueBuilder
                .durable(QueueConstants.DOCUMENT_PARSE_DELAY_QUEUE)
                // 延迟队列的消息过期后路由回文档处理交换机
                .deadLetterExchange(QueueConstants.DOCUMENT_PROCESS_EXCHANGE)
                .deadLetterRoutingKey(QueueConstants.DOCUMENT_PARSE_ROUTING_KEY)
                // 延迟时间：30秒
                .ttl(QueueConstants.DOCUMENT_PARSE_DELAY.intValue())
                .build();
    }

    /**
     * 绑定：文档解析队列 -> 文档处理交换机
     * RoutingKey: document.parse
     */
    @Bean
    public Binding documentParseBinding() {
        return BindingBuilder
                .bind(documentParseQueue())
                .to(documentProcessExchange())
                .with(QueueConstants.DOCUMENT_PARSE_ROUTING_KEY);
    }

    /**
     * 绑定：文档解析死信队列 -> 死信交换机
     * RoutingKey: document.parse.dlq
     */
    @Bean
    public Binding documentParseDLQBinding() {
        return BindingBuilder
                .bind(documentParseDLQ())
                .to(deadLetterExchange())
                .with(QueueConstants.DOCUMENT_PARSE_DLQ_ROUTING_KEY);
    }

    /**
     * 绑定：文档解析延迟队列 -> 死信交换机
     * 用于接收失败的消息并延迟重试
     */
    @Bean
    public Binding documentParseDelayBinding() {
        return BindingBuilder
                .bind(documentParseDelayQueue())
                .to(deadLetterExchange())
                .with(QueueConstants.DOCUMENT_PARSE_ROUTING_KEY);
    }
}