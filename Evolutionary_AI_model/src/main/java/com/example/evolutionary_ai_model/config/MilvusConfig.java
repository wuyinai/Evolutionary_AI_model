package com.example.evolutionary_ai_model.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus向量数据库配置类
 * 配置MilvusClientV2客户端，用于向量存储和检索操作
 */
@Configuration
public class MilvusConfig {

    private static final Logger logger = LoggerFactory.getLogger(MilvusConfig.class);

    @Value("${spring.ai.vectorstore.milvus.client.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.milvus.client.port:19530}")
    private Integer port;

    @Value("${spring.ai.vectorstore.milvus.client.username:root}")
    private String username;

    @Value("${spring.ai.vectorstore.milvus.client.password:milvus}")
    private String password;

    @Value("${spring.ai.vectorstore.milvus.database-name:default}")
    private String databaseName;

    /**
     * 创建MilvusClientV2客户端Bean
     * MilvusClientV2是Milvus SDK v2的客户端，提供更简洁的API和更好的性能
     */
    @Bean
    public MilvusClientV2 milvusClientV2() {
        try {
            // 构建连接配置
            String uri = "http://" + host + ":" + port;
            String token = username + ":" + password;

            ConnectConfig connectConfig = ConnectConfig.builder()
                    .uri(uri)
                    .token(token)
                    .dbName(databaseName)
                    .build();

            // 创建客户端
            MilvusClientV2 client = new MilvusClientV2(connectConfig);

            logger.info("MilvusClientV2创建成功，连接地址: {}, 数据库: {}", uri, databaseName);

            return client;
        } catch (Exception e) {
            logger.error("创建MilvusClientV2失败", e);
            throw new RuntimeException("创建MilvusClientV2失败: " + e.getMessage());
        }
    }
}