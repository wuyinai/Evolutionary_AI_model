package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.DocumentChunk;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 向量存储服务接口，负责将文本向量存储到向量数据库。
 */
public interface VectorStoreService {

    /**
     * 存储文档分块的向量
     * @param chunks 文档分块列表
     * @param embeddingModelId 向量模型配置ID
     * @return 存储的向量ID列表
     */
    List<String> storeVectors(List<DocumentChunk> chunks, Long embeddingModelId);

    /**
     * 相似度搜索
     * @param query 查询文本
     * @param embeddingModelId 向量模型配置ID
     * @param topK 返回的最相似文档数量
     * @return 相似文档列表
     */
    List<Document> similaritySearch(String query, Long embeddingModelId, int topK);

    /**
     * 删除文档的所有向量
     * @param documentId 文档ID
     */
    void deleteByDocumentId(Long documentId);

    /**
     * 删除指定向量
     * @param vectorIds 向量ID列表
     */
    void deleteByIds(List<String> vectorIds);
}
