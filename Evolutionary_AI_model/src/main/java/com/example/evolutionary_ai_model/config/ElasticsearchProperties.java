package com.example.evolutionary_ai_model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用法：Elasticsearch属性配置类，从application.yml读取Elasticsearch连接参数。
 * 位于配置层，提供类型安全的配置属性访问。
 * 采用配置属性模式，统一管理Elasticsearch连接参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class ElasticsearchProperties {

    /**
     * Elasticsearch服务器地址
     */
    private String host = "localhost";

    /**
     * Elasticsearch服务器端口
     */
    private Integer port = 9200;

    /**
     * Elasticsearch用户名（可选）
     */
    private String username;

    /**
     * Elasticsearch密码（可选）
     */
    private String password;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout = 5000;

    /**
     * Socket超时时间（毫秒）
     */
    private Integer socketTimeout = 30000;

    /**
     * 最大连接数
     */
    private Integer maxConnections = 100;

    /**
     * 最大连接路由数（每个路由的最大连接数）
     */
    private Integer maxConnectionsPerRoute = 20;

    /**
     * 索引前缀（用于区分不同环境）
     */
    private String indexPrefix = "evolutionary_ai";

    /**
     * 是否启用SSL
     */
    private Boolean sslEnabled = false;

    /**
     * 是否启用基本认证
     */
    private Boolean basicAuthEnabled = false;
}