package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.service.KnowledgeDocumentService;
import com.example.evolutionary_ai_model.service.RagService;
import com.example.evolutionary_ai_model.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG服务实现类
 * 负责从知识库中检索相关内容并构建增强提示词
 */
@Service
public class RagServiceImpl implements RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagServiceImpl.class);

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @Override
    public List<String> retrieveRelevantContent(List<Long> knowledgeDocumentIds, String query, int topK) {
        logger.info("=== 开始RAG检索 ===");
        logger.info("知识库文档ID列表: {}", knowledgeDocumentIds);
        logger.info("查询内容: {}", query);
        logger.info("topK: {}", topK);

        if (knowledgeDocumentIds == null || knowledgeDocumentIds.isEmpty()) {
            logger.info("知识库文档ID列表为空，跳过RAG检索");
            return new ArrayList<>();
        }

        if (!StringUtils.hasText(query)) {
            logger.warn("查询内容为空，无法进行RAG检索");
            return new ArrayList<>();
        }

        List<String> allRelevantContent = new ArrayList<>();

        try {
            // 获取所有文档信息
            List<KnowledgeDocument> documents = knowledgeDocumentService.listByIds(knowledgeDocumentIds);
            logger.info("查询到的文档数量: {}", documents != null ? documents.size() : 0);

            if (documents == null || documents.isEmpty()) {
                logger.warn("未找到有效的知识库文档，文档ID列表: {}", knowledgeDocumentIds);
                return new ArrayList<>();
            }

            // 过滤出状态为COMPLETED的文档
            List<KnowledgeDocument> completedDocuments = documents.stream()
                    .filter(doc -> "COMPLETED".equals(doc.getStatus()))
                    .collect(Collectors.toList());

            logger.info("状态为COMPLETED的文档数量: {}", completedDocuments.size());

            if (completedDocuments.isEmpty()) {
                logger.warn("没有已完成的知识库文档可用，文档ID列表: {}", knowledgeDocumentIds);
                return new ArrayList<>();
            }

            // 打印每个文档的详细信息
            for (KnowledgeDocument doc : completedDocuments) {
                logger.info("文档详情 - ID: {}, 名称: {}, 向量模型ID: {}, 分块数: {}",
                        doc.getId(), doc.getDocumentName(), doc.getEmbeddingModelId(), doc.getChunkCount());
            }

            // 按向量模型ID分组
            Map<Long, List<KnowledgeDocument>> documentsByEmbeddingModel = completedDocuments.stream()
                    .collect(Collectors.groupingBy(KnowledgeDocument::getEmbeddingModelId));

            logger.info("按向量模型分组后的组数: {}", documentsByEmbeddingModel.size());

            // 对每个向量模型进行检索
            for (Map.Entry<Long, List<KnowledgeDocument>> entry : documentsByEmbeddingModel.entrySet()) {
                Long embeddingModelId = entry.getKey();
                List<KnowledgeDocument> modelDocuments = entry.getValue();

                // 计算每个文档应该检索的数量（按比例分配）
                int docCount = modelDocuments.size();
                int topKPerModel = Math.max(1, topK / documentsByEmbeddingModel.size());

                logger.info("开始RAG检索，向量模型ID: {}, 文档数量: {}, 检索数量: {}",
                        embeddingModelId, docCount, topKPerModel);

                try {
                    // 使用向量存储服务进行相似度搜索
                    List<Document> searchResults = vectorStoreService.similaritySearch(
                            query, embeddingModelId, topKPerModel);

                    logger.info("向量模型 {} 检索返回结果数: {}", embeddingModelId, searchResults.size());

                    // 提取文档内容
                    for (Document doc : searchResults) {
                        String content = doc.getText();
                        if (StringUtils.hasText(content)) {
                            allRelevantContent.add(content);
                            logger.debug("检索到相关内容，长度: {}", content.length());
                        }
                    }

                    logger.info("向量模型 {} 检索完成，返回 {} 条相关内容", embeddingModelId, searchResults.size());

                } catch (Exception e) {
                    logger.error("向量模型 {} 检索失败: {}", embeddingModelId, e.getMessage(), e);
                    // 继续处理其他向量模型，不中断整个流程
                }
            }

            logger.info("RAG检索完成，总共返回 {} 条相关内容", allRelevantContent.size());
            logger.info("=== RAG检索结束 ===");
            return allRelevantContent;

        } catch (Exception e) {
            logger.error("RAG检索异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public String buildRagPrompt(String originalPrompt, List<String> relevantContent) {
        if (relevantContent == null || relevantContent.isEmpty()) {
            logger.info("没有相关内容，返回原始提示词");
            return originalPrompt;
        }

        StringBuilder ragPrompt = new StringBuilder();

        // 添加知识库上下文
        ragPrompt.append("以下是知识库中的相关内容，请基于这些内容回答用户的问题：\n\n");
        ragPrompt.append("=== 知识库内容 ===\n");

        for (int i = 0; i < relevantContent.size(); i++) {
            ragPrompt.append("[文档").append(i + 1).append("]\n");
            ragPrompt.append(relevantContent.get(i)).append("\n\n");
        }

        ragPrompt.append("=== 知识库内容结束 ===\n\n");

        // 添加原始提示词
        ragPrompt.append("用户问题：\n").append(originalPrompt);

        logger.info("构建RAG增强提示词完成，知识库内容数量: {}", relevantContent.size());
        return ragPrompt.toString();
    }

    @Override
    public boolean checkDocumentsAvailable(List<Long> knowledgeDocumentIds) {
        if (knowledgeDocumentIds == null || knowledgeDocumentIds.isEmpty()) {
            return false;
        }

        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.listByIds(knowledgeDocumentIds);
            if (documents == null || documents.isEmpty()) {
                return false;
            }

            // 检查是否所有文档都已完成处理
            return documents.stream()
                    .allMatch(doc -> "COMPLETED".equals(doc.getStatus()));

        } catch (Exception e) {
            logger.error("检查知识库文档可用性失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
