package com.example.evolutionary_ai_model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 用法：RabbitMQ核心配置类，配置连接工厂、消息转换器和RabbitTemplate。
 * 位于配置层，负责RabbitMQ基础设施的依赖注入配置。
 * 采用工厂模式，统一管理RabbitMQ连接和消息处理策略。
 */
@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    /**
     * 配置RabbitMQ连接工厂
     * 支持自定义连接参数、超时设置和连接池配置
     *
     * @return CachingConnectionFactory 缓存连接工厂实例
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        // 设置连接参数
        connectionFactory.setHost(rabbitMQProperties.getHost());
        connectionFactory.setPort(rabbitMQProperties.getPort());
        connectionFactory.setUsername(rabbitMQProperties.getUsername());
        connectionFactory.setPassword(rabbitMQProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitMQProperties.getVirtualHost());

        // 设置连接超时
        connectionFactory.setConnectionTimeout(rabbitMQProperties.getConnectionTimeout());

        // 启用发布确认机制，确保消息到达交换机
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

        // 启用发布返回机制，确保消息从交换机路由到队列失败时能被感知
        connectionFactory.setPublisherReturns(true);

        // 设置连接缓存大小
        connectionFactory.setConnectionCacheSize(25);

        logger.info("RabbitMQ连接工厂初始化完成，主机: {}，端口: {}",
                    rabbitMQProperties.getHost(), rabbitMQProperties.getPort());

        return connectionFactory;
    }

    /**
     * 配置JSON消息转换器
     * 支持Java 8日期时间类型，统一消息序列化格式
     *
     * @return MessageConverter 消息转换器实例
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册JavaTimeModule以支持LocalDateTime等Java 8日期时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳的特性，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略null值
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 忽略未知属性，避免反序列化失败
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 配置RabbitTemplate
     * 提供消息发送模板，支持消息确认、返回和重试机制
     *
     * @param connectionFactory 连接工厂
     * @param messageConverter 消息转换器
     * @return RabbitTemplate 消息模板实例
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                        MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 设置消息转换器
        rabbitTemplate.setMessageConverter(messageConverter);

        // 设置消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                logger.debug("消息发送成功，CorrelationData: {}", correlationData);
            } else {
                logger.error("消息发送失败，CorrelationData: {}，原因: {}", correlationData, cause);
            }
        });

        // 设置消息返回回调（当消息无法路由到队列时触发）
        rabbitTemplate.setReturnsCallback(returned -> {
            logger.error("消息无法路由到队列，Exchange: {}，RoutingKey: {}，消息: {}，回复码: {}，回复文本: {}",
                        returned.getExchange(),
                        returned.getRoutingKey(),
                        returned.getMessage(),
                        returned.getReplyCode(),
                        returned.getReplyText());
        });

        // 配置重试模板
        if (rabbitMQProperties.getRetryEnabled()) {
            rabbitTemplate.setRetryTemplate(buildRetryTemplate());
        }

        logger.info("RabbitTemplate配置完成");

        return rabbitTemplate;
    }

    /**
     * 配置RabbitListener容器工厂
     * 设置消费者并发数、预取数量和确认模式
     *
     * @param connectionFactory 连接工厂
     * @param messageConverter 消息转换器
     * @return SimpleRabbitListenerContainerFactory 监听器容器工厂实例
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        // 设置连接工厂
        factory.setConnectionFactory(connectionFactory);

        // 设置消息转换器
        factory.setMessageConverter(messageConverter);

        // 设置消费者并发数
        factory.setConcurrentConsumers(rabbitMQProperties.getConcurrency());
        factory.setMaxConcurrentConsumers(rabbitMQProperties.getMaxConcurrency());

        // 设置预取数量（控制消费者一次性获取的消息数量）
        factory.setPrefetchCount(rabbitMQProperties.getPrefetch());

        // 设置消息确认模式
        AcknowledgeMode acknowledgeMode = getAcknowledgeMode(rabbitMQProperties.getAcknowledgeMode());
        factory.setAcknowledgeMode(acknowledgeMode);

        logger.info("RabbitListener容器工厂配置完成，并发数: {}，最大并发数: {}，确认模式: {}",
                    rabbitMQProperties.getConcurrency(),
                    rabbitMQProperties.getMaxConcurrency(),
                    acknowledgeMode);

        return factory;
    }

    /**
     * 构建重试模板
     * 配置指数退避策略和最大重试次数
     *
     * @return RetryTemplate 重试模板实例
     */
    private RetryTemplate buildRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 配置指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(rabbitMQProperties.getRetryInitialInterval());
        backOffPolicy.setMaxInterval(rabbitMQProperties.getRetryMaxInterval());
        backOffPolicy.setMultiplier(rabbitMQProperties.getRetryMultiplier());

        // 配置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(rabbitMQProperties.getRetryMaxAttempts());

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        logger.info("RabbitMQ重试模板配置完成，最大重试次数: {}", rabbitMQProperties.getRetryMaxAttempts());

        return retryTemplate;
    }

    /**
     * 获取消息确认模式
     *
     * @param mode 确认模式字符串
     * @return AcknowledgeMode 确认模式枚举
     */
    private AcknowledgeMode getAcknowledgeMode(String mode) {
        return switch (mode.toLowerCase()) {
            case "manual" -> AcknowledgeMode.MANUAL;
            case "none" -> AcknowledgeMode.NONE;
            default -> AcknowledgeMode.AUTO;
        };
    }
}