package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档分块实体类，存储文档分块后的文本片段。
 * 位于数据访问层，映射数据库表 document_chunk。
 */
@Data
@TableName("document_chunk")
public class DocumentChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 文档ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long documentId;

    // 知识库ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeBaseId;

    // 用户ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // 分块序号
    private Integer chunkIndex;

    // 分块内容
    private String content;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long securityLabelId;

    // 向量ID（在向量数据库中的ID）
    private String vectorId;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 删除标记
    @TableLogic
    private Integer delFlag;
}
