package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：知识库密级标签实体类，存储密级标签基本信息。
 * 对应表 knowledge_security_label，用于定义知识库的安全密级（普通、内部、机密、绝密）。
 * 与 SysRole、KnowledgeDocument、DocumentChunk 关联，用于控制访问权限。
 */
@Data
@TableName("knowledge_security_label")
public class KnowledgeSecurityLabel implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（雪花算法）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 标签名称（如：普通、内部、机密、绝密）
    private String labelName;

    // 标签编码（如：NORMAL, INTERNAL, SECRET, TOP_SECRET）
    private String labelCode;

    // 密级等级（数值越大密级越高）
    private Integer labelLevel;

    // 标签描述
    private String description;

    // 创建人
    private String createBy;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新人
    private String updateBy;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除标记：0-未删除 1-已删除
    @TableLogic
    private Integer delFlag;
}
