package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.dto.DocumentChunkDTO;

import java.util.List;

/**
 * RAG（检索增强生成）服务接口
 * 负责从知识库中检索相关内容并构建增强提示词
 */
public interface RagService {

    /**
     * 根据知识库文档ID列表检索相关内容
     * @param knowledgeDocumentIds 知识库文档ID列表
     * @param query 用户查询
     * @param topK 返回的最相关文档片段数量
     * @return 检索到的相关内容列表
     */
    List<String> retrieveRelevantContent(List<Long> knowledgeDocumentIds, String query, int topK);

    /**
     * 根据知识库文档ID列表检索相关文档块详细信息
     * @param knowledgeDocumentIds 知识库文档ID列表
     * @param query 用户查询
     * @param topK 返回的最相关文档片段数量
     * @return 检索到的文档块详细信息列表
     */
    List<DocumentChunkDTO> retrieveRelevantChunks(List<Long> knowledgeDocumentIds, String query, int topK);

    /**
     * 根据知识库ID列表检索相关文档块详细信息（知识库挂载）
     * 会自动查找知识库下所有文档，然后检索文档块
     * @param knowledgeBaseIds 知识库ID列表
     * @param query 用户查询
     * @param topK 返回的最相关文档片段数量
     * @return 检索到的文档块详细信息列表
     */
    List<DocumentChunkDTO> retrieveRelevantChunksByKnowledgeBaseIds(List<Long> knowledgeBaseIds, String query, int topK);

    /**
     * 构建RAG增强提示词
     * @param originalPrompt 原始提示词
     * @param relevantContent 检索到的相关内容
     * @return 增强后的提示词
     */
    String buildRagPrompt(String originalPrompt, List<String> relevantContent);

    /**
     * 检查知识库文档是否可用
     * @param knowledgeDocumentIds 知识库文档ID列表
     * @return 是否所有文档都可用
     */
    boolean checkDocumentsAvailable(List<Long> knowledgeDocumentIds);
}
