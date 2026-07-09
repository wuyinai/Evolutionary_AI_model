package com.example.evolutionary_ai_model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用法：RabbitMQ属性配置类，从application.yml读取RabbitMQ连接参数。
 * 位于配置层，提供类型安全的配置属性访问。
 * 采用配置属性模式，统一管理RabbitMQ连接参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMQProperties {

    /**
     * RabbitMQ服务器地址
     */
    private String host = "localhost";

    /**
     * RabbitMQ服务器端口
     */
    private Integer port = 5672;

    /**
     * RabbitMQ用户名
     */
    private String username;

    /**
     * RabbitMQ密码
     */
    private String password;

    /**
     * 虚拟主机
     */
    private String virtualHost = "/";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout = 60000;

    /**
     * 消息确认模式：none, auto, manual
     */
    private String acknowledgeMode = "auto";

    /**
     * 消费者并发数
     */
    private Integer concurrency = 3;

    /**
     * 消费者最大并发数
     */
    private Integer maxConcurrency = 10;

    /**
     * 预取数量
     */
    private Integer prefetch = 1;

    /**
     * 是否开启消息重试
     */
    private Boolean retryEnabled = true;

    /**
     * 重试初始间隔（毫秒）
     */
    private Long retryInitialInterval = 1000L;

    /**
     * 重试最大间隔（毫秒）
     */
    private Long retryMaxInterval = 10000L;

    /**
     * 重试乘数
     */
    private Double retryMultiplier = 2.0;

    /**
     * 最大重试次数
     */
    private Integer retryMaxAttempts = 3;
}