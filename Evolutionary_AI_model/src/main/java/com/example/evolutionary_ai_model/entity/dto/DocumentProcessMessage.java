package com.example.evolutionary_ai_model.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：文档处理消息DTO，用于RabbitMQ消息传递。
 * 位于数据传输层，封装文档处理任务的所有必要信息。
 * 采用DTO模式，解耦消息队列与业务逻辑。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 用户ID（用于权限校验）
     */
    private Long userId;

    /**
     * 知识库ID（可选）
     */
    private Long knowledgeBaseId;

    /**
     * 向量模型配置ID
     */
    private Long embeddingModelId;

    /**
     * 密级标签ID
     */
    private Long securityLabelId;

    /**
     * 文档存储路径（MinIO路径）
     */
    private String storagePath;

    /**
     * 文件类型（PDF、DOCX、TXT等）
     */
    private String fileType;

    /**
     * 文档名称
     */
    private String documentName;

    /**
     * 重试次数（用于失败重试机制）
     */
    private Integer retryCount;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 构造文档处理消息（首次发送）
     */
    public DocumentProcessMessage(Long documentId, Long userId, Long knowledgeBaseId,
                                   Long embeddingModelId, Long securityLabelId,
                                   String storagePath, String fileType, String documentName) {
        this.documentId = documentId;
        this.userId = userId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.embeddingModelId = embeddingModelId;
        this.securityLabelId = securityLabelId;
        this.storagePath = storagePath;
        this.fileType = fileType;
        this.documentName = documentName;
        this.retryCount = 0;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 增加重试次数并返回是否可以继续重试
     */
    public boolean incrementRetry() {
        this.retryCount++;
        return this.retryCount <= MAX_RETRY_COUNT;
    }

    /**
     * 是否还能重试
     */
    public boolean canRetry() {
        return this.retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 获取剩余重试次数
     */
    public int getRemainingRetries() {
        return MAX_RETRY_COUNT - this.retryCount;
    }
}