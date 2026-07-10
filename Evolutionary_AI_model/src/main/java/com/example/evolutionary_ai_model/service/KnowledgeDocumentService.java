package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档服务接口，负责文档上传、解析、分块、向量化等完整流程。
 */
public interface KnowledgeDocumentService extends IService<KnowledgeDocument> {

    /**
     * 上传并处理文档
     * @param file 上传的文件
     * @param userId 用户ID
     * @param embeddingModelId 向量模型配置ID
     * @param securityLabelId 密级标签ID（必选）
     * @return 文档ID
     */
    Long uploadAndProcessDocument(MultipartFile file, Long userId, Long embeddingModelId, Long securityLabelId);

    /**
     * 上传文档到指定知识库
     * @param file 上传的文件
     * @param userId 用户ID
     * @param knowledgeBaseId 知识库ID
     * @param embeddingModelId 向量模型配置ID（可选，使用知识库默认模型）
     * @param securityLabelId 密级标签ID（必选）
     * @return 文档ID
     */
    Long uploadDocumentToKnowledgeBase(MultipartFile file, Long userId, Long knowledgeBaseId, Long embeddingModelId, Long securityLabelId);

    /**
     * 处理文档（解析、分块、向量化）
     * @param documentId 文档ID
     */
    void processDocument(Long documentId);

    /**
     * 获取用户的文档列表
     * @param userId 用户ID
     * @return 文档列表
     */
    List<KnowledgeDocument> listByUserId(Long userId);

    /**
     * 获取用户的独立文档列表（不属于任何知识库）
     * @param userId 用户ID
     * @return 独立文档列表
     */
    List<KnowledgeDocument> listStandaloneDocuments(Long userId);

    /**
     * 删除文档（包括MinIO文件、向量、数据库记录）
     * @param documentId 文档ID
     */
    void deleteDocument(Long documentId);

    /**
     * 获取文档处理状态
     * @param documentId 文档ID
     * @return 文档信息
     */
    KnowledgeDocument getDocumentStatus(Long documentId);
}
