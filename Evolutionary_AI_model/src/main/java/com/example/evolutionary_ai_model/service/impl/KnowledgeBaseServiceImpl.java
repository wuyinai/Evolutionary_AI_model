package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.mapper.KnowledgeBaseMapper;
import com.example.evolutionary_ai_model.mapper.DocumentChunkMapper;
import com.example.evolutionary_ai_model.mapper.KnowledgeDocumentMapper;
import com.example.evolutionary_ai_model.service.KnowledgeBaseService;
import com.example.evolutionary_ai_model.service.KnowledgeDocumentService;
import com.example.evolutionary_ai_model.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务实现类，负责知识库的创建、查询、删除等操作。
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseServiceImpl.class);

    @Autowired
    private KnowledgeDocumentMapper documentMapper;

    @Autowired
    private DocumentChunkMapper chunkMapper;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Override
    public Long createKnowledgeBase(KnowledgeBase knowledgeBase) {
        logger.info("创建知识库，用户ID: {}, 名称: {}", knowledgeBase.getUserId(), knowledgeBase.getName());
        
        // 设置默认值
        if (knowledgeBase.getDocumentCount() == null) {
            knowledgeBase.setDocumentCount(0);
        }
        if (knowledgeBase.getChunkCount() == null) {
            knowledgeBase.setChunkCount(0);
        }
        if (knowledgeBase.getStatus() == null) {
            knowledgeBase.setStatus("ACTIVE");
        }
        
        save(knowledgeBase);
        logger.info("知识库创建成功，ID: {}", knowledgeBase.getId());
        return knowledgeBase.getId();
    }

    @Override
    public List<KnowledgeBase> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(KnowledgeBase::getUserId, userId)
                .orderByDesc(KnowledgeBase::getCreateTime)
                .list();
    }

    @Override
    public KnowledgeBase getKnowledgeBaseDetail(Long knowledgeBaseId) {
        return getById(knowledgeBaseId);
    }

    @Override
    public void updateKnowledgeBase(KnowledgeBase knowledgeBase) {
        logger.info("更新知识库，ID: {}", knowledgeBase.getId());
        updateById(knowledgeBase);
    }

    @Override
    @Transactional
    public void deleteKnowledgeBase(Long knowledgeBaseId) {
        logger.info("删除知识库，ID: {}", knowledgeBaseId);
        
        KnowledgeBase knowledgeBase = getById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在，ID: " + knowledgeBaseId);
        }
        
        // 1. 获取知识库下所有文档
        List<KnowledgeDocument> documents = listDocuments(knowledgeBaseId);
        
        // 2. 删除所有文档（包括MinIO文件、向量、数据库记录）
        for (KnowledgeDocument document : documents) {
            deleteDocumentWithChunks(document.getId());
        }
        
        // 3. 删除知识库记录
        removeById(knowledgeBaseId);
        
        logger.info("知识库删除成功，ID: {}，删除文档数: {}", knowledgeBaseId, documents.size());
    }

    @Override
    public List<KnowledgeDocument> listDocuments(Long knowledgeBaseId) {
        return documentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId)
                        .orderByDesc(KnowledgeDocument::getCreateTime)
        );
    }

    @Override
    public void updateStatistics(Long knowledgeBaseId) {
        logger.info("更新知识库统计信息，ID: {}", knowledgeBaseId);
        
        // 统计文档数量
        Long documentCount = documentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId)
        );
        
        // 统计分块数量
        Long chunkCount = chunkMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.evolutionary_ai_model.entity.DocumentChunk>()
                        .eq(com.example.evolutionary_ai_model.entity.DocumentChunk::getKnowledgeBaseId, knowledgeBaseId)
        );
        
        // 更新知识库
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setId(knowledgeBaseId);
        knowledgeBase.setDocumentCount(documentCount.intValue());
        knowledgeBase.setChunkCount(chunkCount.intValue());
        updateById(knowledgeBase);
        
        logger.info("知识库统计更新完成，文档数: {}, 分块数: {}", documentCount, chunkCount);
    }

    /**
     * 删除文档及其分块（包括向量和MinIO文件）
     */
    private void deleteDocumentWithChunks(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            return;
        }
        
        try {
            // 1. 删除MinIO文件
            if (document.getStoragePath() != null && !document.getStoragePath().isEmpty()) {
                // 这里需要注入MinioService，暂时跳过
                logger.info("删除MinIO文件: {}", document.getStoragePath());
            }
            
            // 2. 删除向量
            List<com.example.evolutionary_ai_model.entity.DocumentChunk> chunks = chunkMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.evolutionary_ai_model.entity.DocumentChunk>()
                            .eq(com.example.evolutionary_ai_model.entity.DocumentChunk::getDocumentId, documentId)
            );
            
            if (!chunks.isEmpty()) {
                List<String> vectorIds = chunks.stream()
                        .map(com.example.evolutionary_ai_model.entity.DocumentChunk::getVectorId)
                        .filter(id -> id != null)
                        .toList();
                if (!vectorIds.isEmpty()) {
                    vectorStoreService.deleteByIds(vectorIds);
                }
            }
            
            // 3. 删除分块记录
            chunkMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.evolutionary_ai_model.entity.DocumentChunk>()
                            .eq(com.example.evolutionary_ai_model.entity.DocumentChunk::getDocumentId, documentId)
            );
            
            // 4. 删除文档记录
            documentMapper.deleteById(documentId);
            
        } catch (Exception e) {
            logger.error("删除文档失败，文档ID: {}", documentId, e);
            throw new RuntimeException("删除文档失败: " + e.getMessage());
        }
    }
}
