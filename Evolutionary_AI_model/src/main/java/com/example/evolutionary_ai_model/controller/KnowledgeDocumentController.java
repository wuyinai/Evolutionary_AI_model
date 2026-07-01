package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.service.KnowledgeDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档Controller，负责接收前端文档上传和管理请求。
 * 位于表现层，提供文档上传、查询、删除等功能。
 */
@RestController
@RequestMapping("/knowledge/document")
public class KnowledgeDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDocumentController.class);

    private final KnowledgeDocumentService documentService;

    public KnowledgeDocumentController(KnowledgeDocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 上传文档
     * 请求地址: POST /knowledge/document/upload
     * 参数: file - 上传的文件, embeddingModelId - 向量模型配置ID
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('knowledge:document:upload')")
    public Result<Long> uploadDocument(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam("embeddingModelId") Long embeddingModelId) {
        logger.info("上传文档请求，文件名: {}, 向量模型ID: {}", file.getOriginalFilename(), embeddingModelId);

        try {
            Long userId = getUserId(userDetails);
            Long documentId = documentService.uploadAndProcessDocument(file, userId, embeddingModelId);
            logger.info("文档上传成功，文档ID: {}", documentId);
            return Result.success("文档上传成功", documentId);
        } catch (Exception e) {
            logger.error("文档上传失败", e);
            return Result.fail("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文档到指定知识库
     * 请求地址: POST /knowledge/document/upload-to-base
     * 参数: file - 上传的文件, knowledgeBaseId - 知识库ID, embeddingModelId - 向量模型配置ID（可选）
     */
    @PostMapping("/upload-to-base")
    @PreAuthorize("hasAuthority('knowledge:document:upload')")
    @OperationLog("上传文档到知识库")
    public Result<Long> uploadDocumentToKnowledgeBase(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestParam("file") MultipartFile file,
                                                       @RequestParam("knowledgeBaseId") Long knowledgeBaseId,
                                                       @RequestParam(value = "embeddingModelId", required = false) Long embeddingModelId) {
        logger.info("上传文档到知识库请求，文件名: {}, 知识库ID: {}, 向量模型ID: {}", 
                file.getOriginalFilename(), knowledgeBaseId, embeddingModelId);

        try {
            Long userId = getUserId(userDetails);
            Long documentId = documentService.uploadDocumentToKnowledgeBase(file, userId, knowledgeBaseId, embeddingModelId);
            logger.info("文档上传成功，文档ID: {}", documentId);
            return Result.success("文档上传成功", documentId);
        } catch (Exception e) {
            logger.error("文档上传失败", e);
            return Result.fail("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的文档列表
     * 请求地址: GET /knowledge/document/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('knowledge:document:list')")
    public Result<List<KnowledgeDocument>> listDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取文档列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<KnowledgeDocument> documents = documentService.listByUserId(userId);
            logger.info("获取文档列表成功，数量: {}", documents.size());
            return Result.success(documents);
        } catch (Exception e) {
            logger.error("获取文档列表失败", e);
            return Result.fail("获取文档列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档状态
     * 请求地址: GET /knowledge/document/status/{documentId}
     */
    @GetMapping("/status/{documentId}")
    @PreAuthorize("hasAuthority('knowledge:document:list')")
    public Result<KnowledgeDocument> getDocumentStatus(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable Long documentId) {
        logger.info("获取文档状态请求，文档ID: {}", documentId);

        try {
            KnowledgeDocument document = documentService.getDocumentStatus(documentId);
            if (document == null) {
                return Result.fail("文档不存在");
            }
            logger.info("获取文档状态成功，文档ID: {}, 状态: {}", documentId, document.getStatus());
            return Result.success(document);
        } catch (Exception e) {
            logger.error("获取文档状态失败", e);
            return Result.fail("获取文档状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     * 请求地址: DELETE /knowledge/document/{documentId}
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAuthority('knowledge:document:delete')")
    @OperationLog("删除文档")
    public Result<Void> deleteDocument(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long documentId) {
        logger.info("删除文档请求，文档ID: {}", documentId);

        try {
            documentService.deleteDocument(documentId);
            logger.info("文档删除成功，文档ID: {}", documentId);
            return Result.success();
        } catch (Exception e) {
            logger.error("文档删除失败", e);
            return Result.fail("文档删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的独立文档列表（不属于任何知识库）
     * 请求地址: GET /knowledge/document/standalone
     */
    @GetMapping("/standalone")
    @PreAuthorize("hasAuthority('knowledge:document:list')")
    public Result<List<KnowledgeDocument>> listStandaloneDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取独立文档列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<KnowledgeDocument> documents = documentService.listStandaloneDocuments(userId);
            logger.info("获取独立文档列表成功，数量: {}", documents.size());
            return Result.success(documents);
        } catch (Exception e) {
            logger.error("获取独立文档列表失败", e);
            return Result.fail("获取独立文档列表失败: " + e.getMessage());
        }
    }

    /**
     * 重新处理文档
     * 请求地址: POST /knowledge/document/reprocess/{documentId}
     */
    @PostMapping("/reprocess/{documentId}")
    @PreAuthorize("hasAuthority('knowledge:document:edit')")
    public Result<Void> reprocessDocument(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable Long documentId) {
        logger.info("重新处理文档请求，文档ID: {}", documentId);

        try {
            documentService.processDocument(documentId);
            logger.info("文档重新处理成功，文档ID: {}", documentId);
            return Result.success();
        } catch (Exception e) {
            logger.error("文档重新处理失败", e);
            return Result.fail("文档重新处理失败: " + e.getMessage());
        }
    }

    /**
     * 从UserDetails中获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof com.example.evolutionary_ai_model.security.LoginUserDetails) {
            return ((com.example.evolutionary_ai_model.security.LoginUserDetails) userDetails).getUserId();
        }
        throw new IllegalArgumentException("无法获取用户信息");
    }
}
