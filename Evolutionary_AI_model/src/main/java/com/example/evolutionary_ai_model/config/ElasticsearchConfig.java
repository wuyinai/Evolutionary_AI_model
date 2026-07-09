package com.example.evolutionary_ai_model.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * 用法：Elasticsearch核心配置类，配置客户端连接和操作模板。
 * 位于配置层，负责Elasticsearch基础设施的依赖注入配置。
 * 采用工厂模式，统一管理Elasticsearch连接和操作策略。
 */
@Configuration
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    /**
     * 配置Elasticsearch低级别Rest客户端
     * 支持连接参数、超时设置和认证配置
     *
     * @return RestClient 低级别客户端实例
     */
    @Bean
    public RestClient restClient() {
        // 创建HTTP主机配置
        HttpHost httpHost = new HttpHost(
                elasticsearchProperties.getHost(),
                elasticsearchProperties.getPort(),
                elasticsearchProperties.getSslEnabled() ? "https" : "http"
        );

        RestClientBuilder builder = RestClient.builder(httpHost);

        // 配置连接超时和Socket超时
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(elasticsearchProperties.getConnectionTimeout())
                        .setSocketTimeout(elasticsearchProperties.getSocketTimeout())
        );

        // 配置HttpClient连接池
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            // 设置最大连接数
            httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnections());
            httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnectionsPerRoute());

            // 如果启用基本认证，配置凭据
            if (elasticsearchProperties.getBasicAuthEnabled() &&
                    elasticsearchProperties.getUsername() != null &&
                    elasticsearchProperties.getPassword() != null) {

                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                elasticsearchProperties.getUsername(),
                                elasticsearchProperties.getPassword()
                        )
                );

                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            return httpClientBuilder;
        });

        logger.info("Elasticsearch RestClient初始化完成，主机: {}，端口: {}",
                    elasticsearchProperties.getHost(), elasticsearchProperties.getPort());

        return builder.build();
    }

    /**
     * 配置Elasticsearch传输层
     * 使用Jackson作为JSON映射器
     *
     * @param restClient 低级别客户端
     * @return ElasticsearchTransport 传输层实例
     */
    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // 使用Jackson作为JSON映射器
        return new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );
    }

    /**
     * 配置Elasticsearch高级别客户端
     * 提供类型安全的API操作
     *
     * @param elasticsearchTransport 传输层
     * @return ElasticsearchClient 高级别客户端实例
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport elasticsearchTransport) {
        ElasticsearchClient client = new ElasticsearchClient(elasticsearchTransport);

        logger.info("ElasticsearchClient配置完成");

        return client;
    }

    /**
     * 配置Spring Data Elasticsearch操作模板
     * 提供Repository支持的高级抽象
     *
     * @param elasticsearchClient Elasticsearch客户端
     * @return ElasticsearchOperations 操作模板实例
     */
    @Bean
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchClient elasticsearchClient) {
        ElasticsearchTemplate template = new ElasticsearchTemplate(elasticsearchClient);

        logger.info("ElasticsearchOperations模板配置完成");

        return template;
    }
}