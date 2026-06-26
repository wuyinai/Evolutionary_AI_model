package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：AI角色文档关联实体类，存储角色关联的文档文件信息。
 * 位于数据访问层，映射数据库表 ai_role_document。
 * 包含文档元数据、MinIO存储路径、解析后的文本内容等信息。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
@TableName("ai_role_document")
public class AiRoleDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（雪花算法，序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 角色ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    // 文档名称
    private String documentName;

    // MinIO存储路径
    private String documentPath;

    // 文档类型（pdf/docx/txt）
    private String documentType;

    // 文档大小（字节）
    private Long documentSize;

    // 解析后的文本内容
    private String documentContent;

    // 上传时间
    private LocalDateTime uploadTime;

    // 删除标志：0-未删除 1-已删除
    @TableLogic
    private Integer delFlag;
}