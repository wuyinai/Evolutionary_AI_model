package com.example.evolutionary_ai_model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用法：MinIO配置属性类，用于读取application.yml中的MinIO配置参数。
 * 通过Spring Boot的配置属性绑定机制，自动将minio前缀的配置注入到该类中。
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO服务地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 私密密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;
}