package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库实体类，存储知识库基本信息。
 * 一个知识库可以包含多个文档。
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 知识库名称
    private String name;

    // 知识库描述
    private String description;

    // 用户ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // 默认向量模型配置ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long embeddingModelId;

    // 文档数量
    private Integer documentCount;

    // 总分块数量
    private Integer chunkCount;

    // 知识库状态：ACTIVE-活跃 INACTIVE-停用
    private String status;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

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