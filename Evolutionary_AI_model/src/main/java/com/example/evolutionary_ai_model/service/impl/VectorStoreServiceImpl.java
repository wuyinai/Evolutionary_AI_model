package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.DocumentChunk;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.service.VectorStoreService;
import com.example.evolutionary_ai_model.service.factory.ProviderEmbeddingModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 向量存储服务实现类，使用SimpleVectorStore存储向量。
 * 支持持久化到文件，重启后自动加载。
 */
@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreServiceImpl.class);

    private final ProviderEmbeddingModelFactory embeddingModelFactory;
    private final AiModelConfigService modelConfigService;
    private final AiProviderConfigService providerConfigService;

    // 为每个向量模型配置维护一个独立的VectorStore
    private final Map<Long, VectorStore> vectorStoreMap = new ConcurrentHashMap<>();

    // 持久化文件存储目录
    @Value("${vector.store.path:./vector-store}")
    private String vectorStorePath;

    public VectorStoreServiceImpl(ProviderEmbeddingModelFactory embeddingModelFactory,
                                   AiModelConfigService modelConfigService,
                                   AiProviderConfigService providerConfigService) {
        this.embeddingModelFactory = embeddingModelFactory;
        this.modelConfigService = modelConfigService;
        this.providerConfigService = providerConfigService;
    }

    /**
     * 启动时加载所有持久化的向量数据
     */
    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(vectorStorePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("创建向量存储目录: {}", vectorStorePath);
            }

            // 加载所有已存在的向量存储文件
            File[] files = path.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null && files.length > 0) {
                logger.info("发现 {} 个向量存储文件，开始加载...", files.length);
                // 注意：这里不自动加载，因为需要对应的EmbeddingModel
                // 实际使用时会按需加载
            }
        } catch (Exception e) {
            logger.error("初始化向量存储目录失败", e);
        }
    }

    @Override
    public List<String> storeVectors(List<DocumentChunk> chunks, Long embeddingModelId) {
        if (chunks == null || chunks.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 获取或创建VectorStore
            VectorStore vectorStore = getOrCreateVectorStore(embeddingModelId);

            // 创建Document列表
            List<Document> documents = chunks.stream()
                    .map(chunk -> {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("documentId", chunk.getDocumentId().toString());
                        metadata.put("chunkIndex", chunk.getChunkIndex().toString());
                        metadata.put("userId", chunk.getUserId().toString());
                        return new Document(chunk.getId().toString(), chunk.getContent(), metadata);
                    })
                    .collect(Collectors.toList());

            // 存储向量
            vectorStore.add(documents);

            // 持久化到文件
            saveVectorStore(embeddingModelId, vectorStore);

            // 返回向量ID列表
            List<String> vectorIds = documents.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());

            logger.info("向量存储成功，向量模型ID: {}, 向量数量: {}", embeddingModelId, vectorIds.size());
            return vectorIds;
        } catch (Exception e) {
            logger.error("向量存储失败，向量模型ID: {}", embeddingModelId, e);
            throw new RuntimeException("向量存储失败: " + e.getMessage());
        }
    }

    @Override
    public List<Document> similaritySearch(String query, Long embeddingModelId, int topK) {
        try {
            VectorStore vectorStore = getOrCreateVectorStore(embeddingModelId);

            logger.info("开始相似度搜索，查询: {}, 向量模型ID: {}, topK: {}",
                    query.substring(0, Math.min(50, query.length())), embeddingModelId, topK);

            // 使用 SearchRequest.builder() 构建搜索请求
            List<Document> results = vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .build()
            );

            logger.info("相似度搜索完成，查询: {}, 返回结果数: {}", query, results.size());

            // 打印搜索结果详情
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                String text = doc.getText();
                int length = text != null ? text.length() : 0;
                logger.debug("结果 #{}: ID={}, 内容长度={}", i + 1, doc.getId(), length);
            }

            return results;
        } catch (Exception e) {
            logger.error("相似度搜索失败，查询: {}, 向量模型ID: {}", query, embeddingModelId, e);
            throw new RuntimeException("相似度搜索失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        // SimpleVectorStore不支持按条件删除，这里需要从内存中删除
        // 实际生产环境建议使用支持删除的向量数据库（如Milvus、PGVector等）
        logger.warn("SimpleVectorStore不支持按文档ID删除，请使用支持删除操作的向量数据库");
    }

    @Override
    public void deleteByIds(List<String> vectorIds) {
        // SimpleVectorStore不支持按ID删除
        logger.warn("SimpleVectorStore不支持按ID删除，请使用支持删除操作的向量数据库");
    }

    /**
     * 获取或创建VectorStore
     */
    private VectorStore getOrCreateVectorStore(Long embeddingModelId) {
        return vectorStoreMap.computeIfAbsent(embeddingModelId, id -> {
            try {
                // 获取向量模型配置
                AiModelConfig modelConfig = modelConfigService.getConfigById(id);
                if (modelConfig == null) {
                    throw new RuntimeException("向量模型配置不存在，ID: " + id);
                }

                // 获取供应商配置
                AiProviderConfig providerConfig = providerConfigService.getConfigById(modelConfig.getProviderConfigId());
                if (providerConfig == null) {
                    throw new RuntimeException("供应商配置不存在，ID: " + modelConfig.getProviderConfigId());
                }

                // 创建EmbeddingModel
                EmbeddingModel embeddingModel = embeddingModelFactory.getOrCreateEmbeddingModel(providerConfig, modelConfig);

                // 创建SimpleVectorStore
                SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

                // 尝试加载持久化的数据
                loadVectorStore(id, vectorStore);

                logger.info("创建VectorStore成功，向量模型ID: {}", id);
                return vectorStore;
            } catch (Exception e) {
                logger.error("创建VectorStore失败，向量模型ID: {}", id, e);
                throw new RuntimeException("创建VectorStore失败: " + e.getMessage());
            }
        });
    }

    /**
     * 持久化向量存储到文件
     */
    private void saveVectorStore(Long embeddingModelId, VectorStore vectorStore) {
        try {
            if (vectorStore instanceof SimpleVectorStore) {
                String fileName = vectorStorePath + "/vector-store-" + embeddingModelId + ".json";
                File file = new File(fileName);
                ((SimpleVectorStore) vectorStore).save(file);
                logger.debug("向量存储持久化成功，文件: {}", fileName);
            }
        } catch (Exception e) {
            logger.error("向量存储持久化失败，向量模型ID: {}", embeddingModelId, e);
        }
    }

    /**
     * 从文件加载向量存储
     */
    private void loadVectorStore(Long embeddingModelId, SimpleVectorStore vectorStore) {
        try {
            String fileName = vectorStorePath + "/vector-store-" + embeddingModelId + ".json";
            File file = new File(fileName);
            if (file.exists()) {
                vectorStore.load(file);
                logger.info("向量存储加载成功，文件: {}", fileName);
            } else {
                logger.debug("向量存储文件不存在，跳过加载: {}", fileName);
            }
        } catch (Exception e) {
            logger.error("向量存储加载失败，向量模型ID: {}", embeddingModelId, e);
        }
    }
}
