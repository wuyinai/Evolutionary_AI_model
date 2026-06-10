package com.example.evolutionary_ai_model.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用法：MinIO配置类，负责创建和配置MinioClient Bean。
 * 通过依赖注入MinioProperties获取配置参数，构建MinioClient实例供其他组件使用。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * 创建MinioClient Bean
     *
     * @return MinioClient实例
     */
    @Bean
    public MinioClient minioClient() {
        log.info("初始化MinIO客户端，endpoint: {}", minioProperties.getEndpoint());
        
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}