package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：系统默认提示词实体类，存储系统级别的默认提示词信息。
 * 位于数据访问层，映射数据库表 sys_prompt。
 * 支持文档型和文本型两种提示词类型，用于约束智能体规范。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
@TableName("sys_prompt")
public class SysPrompt implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（雪花算法，序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 提示词名称
    private String promptName;

    // 提示词唯一标识
    private String promptCode;

    // 提示词描述
    private String promptDescription;

    // 提示词类型：DOCUMENT-文档型 TEXT-文本型
    private String promptType;

    // 文档名称（仅文档型）
    private String documentName;

    // MinIO存储路径（仅文档型）
    private String documentPath;

    // 文档类型（仅文档型，pdf/docx/txt）
    private String documentType;

    // 文档大小（字节，仅文档型）
    private Long documentSize;

    // 解析后的文本内容（仅文档型）
    private String documentContent;

    // 纯文本提示词内容（仅文本型）
    private String textContent;

    // 是否启用：0-禁用 1-启用
    private Integer isEnabled;

    // 是否默认提示词：0-否 1-是
    private Integer isDefault;

    // 排序号
    private Integer sortOrder;

    // 上传时间
    private LocalDateTime uploadTime;

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

    // 删除标志：0-未删除 1-已删除
    @TableLogic
    private Integer delFlag;

    // 备注
    private String remark;
}