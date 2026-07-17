package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.evolutionary_ai_model.entity.DocumentChunk;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.mapper.DocumentChunkMapper;
import com.example.evolutionary_ai_model.mapper.KnowledgeDocumentMapper;
import com.example.evolutionary_ai_model.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 知识库文档服务实现类，整合文档上传、解析、分块、向量化等完整流程。
 * 采用流式处理，避免大文件内存溢出问题。
 */
@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDocumentServiceImpl.class);

    // 批量处理大小，每处理这么多分块就进行一次向量化
    private static final int BATCH_SIZE = 10;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private StreamingDocumentProcessor streamingProcessor;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

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

            // 3. 异步处理文档（解析、分块、向量化）
            processDocument(documentId);

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
    @Override
    @Transactional
    public void processDocument(Long documentId) {
        KnowledgeDocument document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在，ID: " + documentId);
        }

        logger.info("开始流式处理文档，文档ID: {}, 文件名: {}", documentId, document.getDocumentName());

        try {
            // 更新状态为处理中
            document.setStatus("PROCESSING");
            updateById(document);

            // 从MinIO下载文件
            InputStream inputStream = minioService.downloadFile(document.getStoragePath());

            // 流式处理：边解析边分块边向量化
            AtomicInteger chunkIndex = new AtomicInteger(0);
            AtomicInteger totalChunks = new AtomicInteger(0);
            List<DocumentChunk> batch = new ArrayList<>();

            // 使用流式处理器
            int processedChunks = streamingProcessor.processStreaming(
                    inputStream,
                    document.getFileType(),
                    500,  // 分块大小
                    50,   // 重叠大小
                    chunkContent -> {
                        try {
                            int currentIndex = chunkIndex.getAndIncrement();
                            totalChunks.incrementAndGet();

                            // 创建分块记录
                            DocumentChunk chunk = new DocumentChunk();
                            chunk.setDocumentId(documentId);
                            chunk.setKnowledgeBaseId(document.getKnowledgeBaseId());
                            chunk.setUserId(document.getUserId());
                            // 设置分块的密级，继承自文档
                            chunk.setSecurityLabelId(document.getSecurityLabelId());
                            chunk.setChunkIndex(currentIndex);
                            chunk.setContent(chunkContent);

                            // 保存分块记录
                            documentChunkMapper.insert(chunk);

                            // 添加到批处理列表
                            batch.add(chunk);

                            // 达到批处理大小时，进行向量化
                            if (batch.size() >= BATCH_SIZE) {
                                processBatch(batch, document.getEmbeddingModelId());
                                batch.clear();
                            }

                            logger.debug("处理分块 #{}, 长度: {}", currentIndex, chunkContent.length());
                        } catch (Exception e) {
                            logger.error("处理分块失败", e);
                            throw new RuntimeException("处理分块失败: " + e.getMessage(), e);
                        }
                    }
            );

            // 处理剩余的分块
            if (!batch.isEmpty()) {
                processBatch(batch, document.getEmbeddingModelId());
            }

            // 更新文档状态
            document.setStatus("COMPLETED");
            document.setChunkCount(processedChunks);
            updateById(document);

            logger.info("文档流式处理完成，文档ID: {}, 总分块数: {}", documentId, processedChunks);

            // 如果文档属于某个知识库，更新知识库的文档数和分块数统计
            if (document.getKnowledgeBaseId() != null) {
                knowledgeBaseService.updateStatistics(document.getKnowledgeBaseId());
            }
        } catch (Exception e) {
            logger.error("文档处理失败，文档ID: {}", documentId, e);
            document.setStatus("FAILED");
            document.setErrorMessage("文档处理失败: " + e.getMessage());
            updateById(document);
            throw new RuntimeException("文档处理失败: " + e.getMessage());
        }
    }

    /**
     * 批量处理分块：向量化并更新向量ID
     */
    private void processBatch(List<DocumentChunk> batch, Long embeddingModelId) {
        if (batch.isEmpty()) {
            return;
        }

        logger.info("批量向量化，分块数: {}", batch.size());

        // 向量化
        List<String> vectorIds = vectorStoreService.storeVectors(batch, embeddingModelId);

        // 更新向量ID
        for (int i = 0; i < batch.size(); i++) {
            DocumentChunk chunk = batch.get(i);
            chunk.setVectorId(vectorIds.get(i));
            documentChunkMapper.updateById(chunk);
        }

        logger.info("批量向量化完成，向量数: {}", vectorIds.size());
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
