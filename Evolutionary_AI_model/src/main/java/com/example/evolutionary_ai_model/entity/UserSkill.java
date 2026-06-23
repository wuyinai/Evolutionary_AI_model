package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：用户技能实体类，负责存储用户上传的技能包信息。
 * 对应数据库表 user_skills，包含技能的基本信息、路径、启用状态等。
 */
@Data
@TableName("user_skills")
public class UserSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String name;

    private String displayName;

    private String description;

    private String version;

    private String author;

    private String path;

    private Boolean enabled;

    private String metadata;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer delFlag;
}