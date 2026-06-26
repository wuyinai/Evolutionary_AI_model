package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用法：AI角色实体类，存储用户自定义的AI角色信息。
 * 位于数据访问层，映射数据库表 ai_role。
 * 包含角色名称、描述、系统提示词等信息，支持文档关联。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
@TableName("ai_role")
public class AiRole implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（雪花算法，序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 角色名称
    private String roleName;

    // 角色唯一标识
    private String roleCode;

    // 角色描述
    private String description;

    // 纯文本系统提示词（可选）
    private String systemPrompt;

    // 系统提示词模板（支持变量替换）
    private String systemPromptTemplate;

    // 创建者用户ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // 是否公开：0-私有 1-公开
    private Integer isPublic;

    // 状态：0-禁用 1-启用
    private Integer status;

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

    // 关联文档列表（非持久化字段，用于展示）
    @TableField(exist = false)
    private List<AiRoleDocument> documents;
}