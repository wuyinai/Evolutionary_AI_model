package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.evolutionary_ai_model.entity.DocumentChunk;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.entity.dto.DocumentProcessMessage;
import com.example.evolutionary_ai_model.mapper.DocumentChunkMapper;
import com.example.evolutionary_ai_model.mapper.KnowledgeDocumentMapper;
import com.example.evolutionary_ai_model.mq.producer.DocumentProducer;
import com.example.evolutionary_ai_model.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档服务实现类，整合文档上传、解析、分块、向量化等完整流程。
 * 采用异步消息队列处理，提高系统响应速度和可靠性。
 */
@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDocumentServiceImpl.class);

    @Autowired
    private MinioService minioService;

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private DocumentProducer documentProducer;

    @Override
    @Transactional
    public Long uploadAndProcessDocument(MultipartFile file, Long userId, Long embeddingModelId, Long securityLabelId) {
        return uploadDocumentToKnowledgeBase(file, userId, null, embeddingModelId, securityLabelId);
    }

    @Override
    @Transactional
    public Long uploadDocumentToKnowledgeBase(MultipartFile file, Long userId, Long knowledgeBaseId, Long embeddingModelId, Long securityLabelId) {
        logger.info("开始上传文档，用户ID: {}, 知识库ID: {}, 文件名: {}, 密级ID: {}", userId, knowledgeBaseId, file.getOriginalFilename(), securityLabelId);

        // 如果指定了知识库，获取知识库的默认向量模型
        if (knowledgeBaseId != null && embeddingModelId == null) {
            KnowledgeBase knowledgeBase = knowledgeBaseService.getById(knowledgeBaseId);
            if (knowledgeBase != null && knowledgeBase.getEmbeddingModelId() != null) {
                embeddingModelId = knowledgeBase.getEmbeddingModelId();
            }
        }

        // 1. 创建文档记录
        KnowledgeDocument document = new KnowledgeDocument();
        document.setDocumentName(file.getOriginalFilename());
        document.setUserId(userId);
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setFileType(documentParserService.getFileType(file.getOriginalFilename()));
        document.setFileSize(file.getSize());
        document.setEmbeddingModelId(embeddingModelId);
        document.setSecurityLabelId(securityLabelId);
        document.setStatus("PENDING");
        document.setChunkCount(0);
        document.setStoragePath(""); // 先设置空字符串，后续更新

        // 保存文档记录
        save(document);
        Long documentId = document.getId();

        try {
            // 2. 上传文件到MinIO
            String storagePath = "documents/" + userId + "/" + documentId + "/" + file.getOriginalFilename();
            minioService.uploadFile(file, storagePath);
            document.setStoragePath(storagePath);
            updateById(document);

            logger.info("文档上传成功，文档ID: {}, 存储路径: {}", documentId, storagePath);

            // 3. 异步处理文档（发送消息到RabbitMQ队列）
            // 创建消息DTO
            DocumentProcessMessage message = new DocumentProcessMessage(
                    documentId,
                    userId,
                    knowledgeBaseId,
                    embeddingModelId,
                    securityLabelId,
                    storagePath,
                    document.getFileType(),
                    file.getOriginalFilename()
            );

            // 发送消息到队列
            documentProducer.sendDocumentProcessMessage(message);

            logger.info("文档处理消息已发送到队列，文档ID: {}", documentId);

            return documentId;
        } catch (Exception e) {
            logger.error("文档上传失败，文档ID: {}", documentId, e);
            document.setStatus("FAILED");
            document.setErrorMessage("文档上传失败: " + e.getMessage());
            updateById(document);
            throw new RuntimeException("文档上传失败: " + e.getMessage());
        }
    }

    //TODO 缺乏企业级的权限管理文档，需要将RAG分块与角色进行绑定，已达到知识库的权限管理
    //TODO 只是用了向量检索缺乏BM25关键词检索。
    //TODO 没有使用RRF计算权重的得分
    /**
     * 处理文档（发送到消息队列异步处理）
     * 注意：现在改为异步处理，通过RabbitMQ消息队列执行
     *
     * @param documentId 文档ID
     */
    @Override
    public void processDocument(Long documentId) {
        KnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在，ID: " + documentId);
        }

        logger.info("发送文档处理消息到队列，文档ID: {}, 文件名: {}", documentId, document.getDocumentName());

        // 创建消息DTO
        DocumentProcessMessage message = new DocumentProcessMessage(
                documentId,
                document.getUserId(),
                document.getKnowledgeBaseId(),
                document.getEmbeddingModelId(),
                document.getSecurityLabelId(),
                document.getStoragePath(),
                document.getFileType(),
                document.getDocumentName()
        );

        // 发送消息到队列
        documentProducer.sendDocumentProcessMessage(message);

        logger.info("文档处理消息已发送，文档ID: {}", documentId);
    }

    @Override
    public List<KnowledgeDocument> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(KnowledgeDocument::getUserId, userId)
                .orderByDesc(KnowledgeDocument::getCreateTime)
                .list();
    }

    @Override
    public List<KnowledgeDocument> listStandaloneDocuments(Long userId) {
        return lambdaQuery()
                .eq(KnowledgeDocument::getUserId, userId)
                .isNull(KnowledgeDocument::getKnowledgeBaseId)
                .orderByDesc(KnowledgeDocument::getCreateTime)
                .list();
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        KnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在，ID: " + documentId);
        }

        logger.info("开始删除文档，文档ID: {}", documentId);

        try {
            // 1. 删除MinIO文件
            if (document.getStoragePath() != null) {
                minioService.deleteFile(document.getStoragePath());
            }

            // 2. 删除向量
            List<DocumentChunk> chunks = documentChunkMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DocumentChunk>()
                            .eq(DocumentChunk::getDocumentId, documentId)
            );

            if (!chunks.isEmpty()) {
                List<String> vectorIds = chunks.stream()
                        .map(DocumentChunk::getVectorId)
                        .filter(id -> id != null)
                        .toList();
                vectorStoreService.deleteByIds(vectorIds);
            }

            // 3. 删除分块记录
            documentChunkMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DocumentChunk>()
                            .eq(DocumentChunk::getDocumentId, documentId)
            );

            // 4. 删除文档记录
            removeById(documentId);

            // 如果文档属于某个知识库，更新知识库统计信息
            if (document.getKnowledgeBaseId() != null) {
                knowledgeBaseService.updateStatistics(document.getKnowledgeBaseId());
            }

            logger.info("文档删除成功，文档ID: {}", documentId);
        } catch (Exception e) {
            logger.error("文档删除失败，文档ID: {}", documentId, e);
            throw new RuntimeException("文档删除失败: " + e.getMessage());
        }
    }

    @Override
    public KnowledgeDocument getDocumentStatus(Long documentId) {
        return getById(documentId);
    }
}
