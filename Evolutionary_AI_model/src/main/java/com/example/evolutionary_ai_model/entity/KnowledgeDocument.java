package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档实体类，存储用户上传的文档信息。
 * 位于数据访问层，映射数据库表 knowledge_document。
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 文档名称
    private String documentName;

    // 用户ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // 知识库ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeBaseId;

    // 文件类型（pdf/docx/txt）
    private String fileType;

    // 文件大小（字节）
    private Long fileSize;

    // MinIO存储路径
    private String storagePath;

    // 向量模型配置ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long embeddingModelId;

    // 文档状态：PENDING-待处理 PROCESSING-处理中 COMPLETED-已完成 FAILED-失败
    private String status;

    // 分块数量
    private Integer chunkCount;

    // 错误信息
    private String errorMessage;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long securityLabelId;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除标记
    @TableLogic
    private Integer delFlag;
}
