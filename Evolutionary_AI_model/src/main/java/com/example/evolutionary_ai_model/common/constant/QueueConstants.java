package com.example.evolutionary_ai_model.common.constant;

/**
 * 用法：消息队列常量类，定义RabbitMQ交换机、队列和路由键的常量。
 * 位于常量层，提供统一的消息队列命名规范，避免硬编码。
 * 采用常量模式，集中管理所有消息队列相关标识。
 */
public class QueueConstants {

    /**
     * 默认交换机名称
     */
    public static final String DEFAULT_EXCHANGE = "evolutionary.ai.default.exchange";

    /**
     * 死信交换机名称
     */
    public static final String DEAD_LETTER_EXCHANGE = "evolutionary.ai.dead.letter.exchange";

    /**
     * 延迟交换机名称（基于死信队列实现延迟消息）
     */
    public static final String DELAY_EXCHANGE = "evolutionary.ai.delay.exchange";

    /**
     * 文档处理交换机
     */
    public static final String DOCUMENT_PROCESS_EXCHANGE = "evolutionary.ai.document.process.exchange";

    /**
     * AI模型调用交换机
     */
    public static final String AI_MODEL_EXCHANGE = "evolutionary.ai.model.exchange";

    /**
     * 文档解析队列
     */
    public static final String DOCUMENT_PARSE_QUEUE = "evolutionary.ai.document.parse.queue";

    /**
     * 文档解析死信队列
     */
    public static final String DOCUMENT_PARSE_DLQ = "evolutionary.ai.document.parse.dlq";

    /**
     * 文档解析延迟队列（用于重试）
     */
    public static final String DOCUMENT_PARSE_DELAY_QUEUE = "evolutionary.ai.document.parse.delay.queue";

    /**
     * AI对话队列
     */
    public static final String AI_CHAT_QUEUE = "evolutionary.ai.chat.queue";

    /**
     * AI对话死信队列
     */
    public static final String AI_CHAT_DLQ = "evolutionary.ai.chat.dlq";

    /**
     * 知识库同步队列
     */
    public static final String KNOWLEDGE_SYNC_QUEUE = "evolutionary.ai.knowledge.sync.queue";

    /**
     * 知识库同步死信队列
     */
    public static final String KNOWLEDGE_SYNC_DLQ = "evolutionary.ai.knowledge.sync.dlq";

    /**
     * 文档解析路由键
     */
    public static final String DOCUMENT_PARSE_ROUTING_KEY = "document.parse";

    /**
     * 文档解析死信路由键
     */
    public static final String DOCUMENT_PARSE_DLQ_ROUTING_KEY = "document.parse.dlq";

    /**
     * AI对话路由键
     */
    public static final String AI_CHAT_ROUTING_KEY = "ai.chat";

    /**
     * AI对话死信路由键
     */
    public static final String AI_CHAT_DLQ_ROUTING_KEY = "ai.chat.dlq";

    /**
     * 知识库同步路由键
     */
    public static final String KNOWLEDGE_SYNC_ROUTING_KEY = "knowledge.sync";

    /**
     * 知识库同步死信路由键
     */
    public static final String KNOWLEDGE_SYNC_DLQ_ROUTING_KEY = "knowledge.sync.dlq";

    /**
     * 消息过期时间（毫秒）：文档解析任务10分钟过期
     */
    public static final Long DOCUMENT_PARSE_EXPIRATION = 600000L;

    /**
     * 消息过期时间（毫秒）：AI对话任务30分钟过期
     */
    public static final Long AI_CHAT_EXPIRATION = 1800000L;

    /**
     * 消息过期时间（毫秒）：知识库同步任务1小时过期
     */
    public static final Long KNOWLEDGE_SYNC_EXPIRATION = 3600000L;

    /**
     * 延迟时间（毫秒）：文档解析失败后延迟重试时间
     */
    public static final Long DOCUMENT_PARSE_DELAY = 30000L;

    /**
     * 延迟时间（毫秒）：AI对话失败后延迟重试时间
     */
    public static final Long AI_CHAT_DELAY = 10000L;
}