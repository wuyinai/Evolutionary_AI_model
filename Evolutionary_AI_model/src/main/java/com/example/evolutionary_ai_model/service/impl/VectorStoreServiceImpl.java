package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.DocumentChunk;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.service.KnowledgeDocumentService;
import com.example.evolutionary_ai_model.service.VectorStoreService;
import com.example.evolutionary_ai_model.service.factory.ProviderEmbeddingModelFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.*;
import io.milvus.v2.service.vector.response.SearchResp.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 向量存储服务实现类，使用Milvus向量数据库存储向量。
 * 为每个向量模型配置维护一个独立的Collection，支持动态创建和管理。
 */
@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreServiceImpl.class);
    private static final Gson gson = new Gson();

    private final ProviderEmbeddingModelFactory embeddingModelFactory;
    private final AiModelConfigService modelConfigService;
    private final AiProviderConfigService providerConfigService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    // Milvus客户端（手动配置的MilvusClientV2）
    @Autowired
    private MilvusClientV2 milvusClient;

    // 为每个向量模型配置维护一个独立的EmbeddingModel和Collection名称
    private final Map<Long, EmbeddingModel> embeddingModelMap = new ConcurrentHashMap<>();
    private final Map<Long, String> collectionNameMap = new ConcurrentHashMap<>();

    // Milvus配置
    @Value("${spring.ai.vectorstore.milvus.database-name:default}")
    private String databaseName;

    @Value("${spring.ai.vectorstore.milvus.metric-type:COSINE}")
    private String metricType;

    public VectorStoreServiceImpl(ProviderEmbeddingModelFactory embeddingModelFactory,
                                  AiModelConfigService modelConfigService,
                                  AiProviderConfigService providerConfigService,
                                  @Lazy KnowledgeDocumentService knowledgeDocumentService) {
        this.embeddingModelFactory = embeddingModelFactory;
        this.modelConfigService = modelConfigService;
        this.providerConfigService = providerConfigService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    /**
     * 启动时检查Milvus连接
     */
    @PostConstruct
    public void init() {
        try {
            if (milvusClient != null) {
                // 测试连接
                ListCollectionsResp listCollectionsResp = milvusClient.listCollections();
                logger.info("Milvus连接成功，当前数据库: {}, 已有Collection数量: {}",
                        databaseName, listCollectionsResp.getCollectionNames().size());
            } else {
                logger.warn("Milvus客户端未注入，请检查配置");
            }
        } catch (Exception e) {
            logger.error("初始化Milvus连接失败", e);
        }
    }

    /**
     * 关闭时清理资源
     */
    @PreDestroy
    public void destroy() {
        try {
            if (milvusClient != null) {
                milvusClient.close();
                logger.info("Milvus客户端已关闭");
            }
        } catch (Exception e) {
            logger.error("关闭Milvus客户端失败", e);
        }
    }

    @Override
    public List<String> storeVectors(List<DocumentChunk> chunks, Long embeddingModelId) {
        if (chunks == null || chunks.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 获取或创建EmbeddingModel和Collection
            EmbeddingModel embeddingModel = getOrCreateEmbeddingModel(embeddingModelId);
            String collectionName = getOrCreateCollection(embeddingModelId);

            // 缓存文档名称，避免重复查询
            Map<Long, String> documentNameCache = new HashMap<>();

            // 生成向量
            List<String> texts = chunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.toList());

            // 使用EmbeddingModel生成向量（embed方法直接返回float[]）
            List<float[]> embeddings = embeddingModel.embed(texts);

            // 构建插入数据
            List<JsonObject> data = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = chunks.get(i);
                float[] embedding = embeddings.get(i);

                // 构建metadata JSON
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("documentId", chunk.getDocumentId().toString());
                metadata.put("chunkIndex", chunk.getChunkIndex().toString());
                metadata.put("userId", chunk.getUserId().toString());
                metadata.put("embeddingModelId", embeddingModelId.toString());

                // 添加文档名称到元数据
                String documentName = documentNameCache.computeIfAbsent(chunk.getDocumentId(), docId -> {
                    KnowledgeDocument doc = knowledgeDocumentService.getById(docId);
                    return doc != null ? doc.getDocumentName() : "未知文档";
                });
                metadata.put("documentName", documentName);

                // 构建JsonObject
                JsonObject row = new JsonObject();
                row.addProperty("id", chunk.getId().toString());
                row.addProperty("content", chunk.getContent());
                row.add("embedding", gson.toJsonTree(embedding));
                row.add("metadata", gson.toJsonTree(metadata));

                data.add(row);
            }

            // 插入数据到Milvus
            InsertReq insertReq = InsertReq.builder()
                    .collectionName(collectionName)
                    .data(data)
                    .build();

            milvusClient.insert(insertReq);

            logger.info("向量存储成功，向量模型ID: {}, 向量数量: {}, Collection: {}",
                    embeddingModelId, chunks.size(), collectionName);

            // 返回向量ID列表
            return chunks.stream()
                    .map(chunk -> chunk.getId().toString())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("向量存储失败，向量模型ID: {}", embeddingModelId, e);
            throw new RuntimeException("向量存储失败: " + e.getMessage());
        }
    }

    @Override
    public List<Document> similaritySearch(String query, Long embeddingModelId, int topK) {
        return similaritySearch(query, embeddingModelId, topK, null);
    }

    @Override
    public List<Document> similaritySearch(String query, Long embeddingModelId, int topK, List<Long> documentIds) {
        try {
            // 获取或创建EmbeddingModel和Collection
            EmbeddingModel embeddingModel = getOrCreateEmbeddingModel(embeddingModelId);
            String collectionName = getOrCreateCollection(embeddingModelId);

            logger.info("开始相似度搜索，查询: {}, 向量模型ID: {}, topK: {}, 文档ID过滤: {}",
                    query.substring(0, Math.min(50, query.length())), embeddingModelId, topK,
                    documentIds != null && !documentIds.isEmpty() ? documentIds : "无");

            // 生成查询向量（embed方法直接返回float[]）
            float[] queryEmbedding = embeddingModel.embed(query);

            // 使用FloatVec包装向量数据（Milvus SDK v2需要BaseVector类型）
            FloatVec floatVec = new FloatVec(queryEmbedding);

            // 构建搜索请求
            SearchReq.SearchReqBuilder searchReqBuilder = SearchReq.builder()
                    .collectionName(collectionName)
                    .data(Arrays.asList(floatVec))
                    .topK(topK)
                    .outputFields(Arrays.asList("id", "content", "metadata"));

            // 如果指定了文档ID列表，添加过滤条件
            if (documentIds != null && !documentIds.isEmpty()) {
                String filterExpression = buildDocumentIdFilter(documentIds);
                searchReqBuilder.filter(filterExpression);
                logger.info("添加文档ID过滤条件: {}", filterExpression);
            }

            // 执行搜索
            SearchResp searchResp = milvusClient.search(searchReqBuilder.build());

            // 转换搜索结果为Document列表
            List<Document> results = new ArrayList<>();

            // SearchResp.getSearchResults()返回List<List<SearchResult>>
            // 每个SearchResult包含entity(Map)和score(Float)
            List<List<SearchResult>> searchResults = searchResp.getSearchResults();

            if (searchResults != null && !searchResults.isEmpty()) {
                // 取第一个查询向量的结果（因为我们只有一个查询向量）
                List<SearchResult> resultList = searchResults.get(0);

                if (resultList != null) {
                    for (SearchResult searchResult : resultList) {
                        // 获取entity中的字段数据
                        Map<String, Object> entity = searchResult.getEntity();

                        if (entity != null) {
                            String id = String.valueOf(entity.get("id"));
                            String content = String.valueOf(entity.get("content"));
                            Object metadataObj = entity.get("metadata");

                            // 解析metadata（处理多种格式）
                            Map<String, Object> metadata = new HashMap<>();
                            if (metadataObj instanceof String) {
                                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                metadata = gson.fromJson((String) metadataObj, type);
                            } else if (metadataObj instanceof Map) {
                                metadata = (Map<String, Object>) metadataObj;
                            } else if (metadataObj instanceof JsonObject) {
                                // 处理JsonObject类型
                                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                metadata = gson.fromJson((JsonObject) metadataObj, type);
                            } else if (metadataObj != null) {
                                // 尝试将其他类型转换为字符串再解析
                                try {
                                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                    metadata = gson.fromJson(metadataObj.toString(), type);
                                } catch (Exception e) {
                                    logger.warn("无法解析metadata: {}", metadataObj);
                                }
                            }

                            // 添加相似度得分到metadata
                            metadata.put("score", searchResult.getScore());

                            // 创建Document对象
                            Document doc = new Document(id, content, metadata);
                            results.add(doc);

                            logger.debug("检索到结果，ID: {}, 相似度得分: {}", id, searchResult.getScore());
                        }
                    }
                }
            }

            logger.info("相似度搜索完成，查询: {}, 返回结果数: {}", query, results.size());

            return results;

        } catch (Exception e) {
            logger.error("相似度搜索失败，查询: {}, 向量模型ID: {}", query, embeddingModelId, e);
            throw new RuntimeException("相似度搜索失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        try {
            // 需要从所有Collection中删除该文档的向量
            for (String collectionName : collectionNameMap.values()) {
                String filterExpression = "metadata['documentId'] == '" + documentId + "'";

                DeleteReq deleteReq = DeleteReq.builder()
                        .collectionName(collectionName)
                        .filter(filterExpression)
                        .build();

                milvusClient.delete(deleteReq);
                logger.info("删除文档向量成功，文档ID: {}, Collection: {}", documentId, collectionName);
            }
        } catch (Exception e) {
            logger.error("删除文档向量失败，文档ID: {}", documentId, e);
            throw new RuntimeException("删除文档向量失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteByIds(List<String> vectorIds) {
        try {
            if (vectorIds == null || vectorIds.isEmpty()) {
                return;
            }

            // 需要从所有Collection中删除这些向量
            for (String collectionName : collectionNameMap.values()) {
                String filterExpression = "id in [" + vectorIds.stream()
                        .map(id -> "'" + id + "'")
                        .collect(Collectors.joining(", ")) + "]";

                DeleteReq deleteReq = DeleteReq.builder()
                        .collectionName(collectionName)
                        .filter(filterExpression)
                        .build();

                milvusClient.delete(deleteReq);
                logger.info("删除向量成功，向量ID数量: {}, Collection: {}", vectorIds.size(), collectionName);
            }
        } catch (Exception e) {
            logger.error("删除向量失败，向量ID数量: {}", vectorIds.size(), e);
            throw new RuntimeException("删除向量失败: " + e.getMessage());
        }
    }

    /**
     * 获取或创建EmbeddingModel
     */
    private EmbeddingModel getOrCreateEmbeddingModel(Long embeddingModelId) {
        return embeddingModelMap.computeIfAbsent(embeddingModelId, id -> {
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

                logger.info("创建EmbeddingModel成功，向量模型ID: {}", id);
                return embeddingModel;
            } catch (Exception e) {
                logger.error("创建EmbeddingModel失败，向量模型ID: {}", id, e);
                throw new RuntimeException("创建EmbeddingModel失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取或创建Collection
     */
    private String getOrCreateCollection(Long embeddingModelId) {
        return collectionNameMap.computeIfAbsent(embeddingModelId, id -> {
            try {
                // 获取向量模型配置
                AiModelConfig modelConfig = modelConfigService.getConfigById(id);
                if (modelConfig == null) {
                    throw new RuntimeException("向量模型配置不存在，ID: " + id);
                }

                // Collection名称：embedding_model_{id}
                String collectionName = "embedding_model_" + id;

                // 检查Collection是否已存在
                HasCollectionReq hasCollectionReq = HasCollectionReq.builder()
                        .collectionName(collectionName)
                        .build();

                Boolean hasCollection = milvusClient.hasCollection(hasCollectionReq);

                if (!hasCollection) {
                    // 创建Collection
                    createCollection(collectionName, modelConfig.getVectorDimensions());
                    logger.info("创建Collection成功，Collection: {}, 向量维度: {}",
                            collectionName, modelConfig.getVectorDimensions());
                } else {
                    // 加载Collection（如果未加载）
                    LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                            .collectionName(collectionName)
                            .build();
                    milvusClient.loadCollection(loadCollectionReq);
                    logger.info("加载已存在的Collection: {}", collectionName);
                }

                return collectionName;
            } catch (Exception e) {
                logger.error("获取或创建Collection失败，向量模型ID: {}", id, e);
                throw new RuntimeException("获取或创建Collection失败: " + e.getMessage());
            }
        });
    }

    /**
     * 创建Collection（使用正确的Milvus SDK v2 API）
     */
    private void createCollection(String collectionName, Integer dimension) {
        try {
            // 创建Schema
            CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();

            // 添加字段到Schema
            schema.addField(AddFieldReq.builder()
                    .fieldName("id")
                    .dataType(DataType.VarChar)
                    .maxLength(256)
                    .isPrimaryKey(true)
                    .autoID(false)
                    .build());

            schema.addField(AddFieldReq.builder()
                    .fieldName("content")
                    .dataType(DataType.VarChar)
                    .maxLength(65535)
                    .build());

            schema.addField(AddFieldReq.builder()
                    .fieldName("embedding")
                    .dataType(DataType.FloatVector)
                    .dimension(dimension)
                    .build());

            schema.addField(AddFieldReq.builder()
                    .fieldName("metadata")
                    .dataType(DataType.JSON)
                    .build());

            // 创建索引参数（extraParams需要Map类型）
            Map<String, Object> extraParams = new HashMap<>();
            extraParams.put("nlist", 1024);

            IndexParam indexParam = IndexParam.builder()
                    .fieldName("embedding")
                    .indexType(IndexParam.IndexType.IVF_FLAT)
                    .metricType(IndexParam.MetricType.valueOf(metricType))
                    .extraParams(extraParams)
                    .build();

            // 创建Collection
            CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                    .collectionName(collectionName)
                    .description("Vector store for embedding model")
                    .collectionSchema(schema)
                    .indexParams(Arrays.asList(indexParam))
                    .build();

            milvusClient.createCollection(createCollectionReq);

            logger.info("Collection创建完成: {}, 向量维度: {}", collectionName, dimension);

        } catch (Exception e) {
            logger.error("创建Collection失败: {}", collectionName, e);
            throw new RuntimeException("创建Collection失败: " + e.getMessage());
        }
    }

    /**
     * 构建文档ID过滤表达式
     */
    private String buildDocumentIdFilter(List<Long> documentIds) {
        return "metadata['documentId'] in [" + documentIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(", ")) + "]";
    }
}