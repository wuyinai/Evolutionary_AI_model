package com.example.evolutionary_ai_model.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用法：用户技能VO，用于返回给前端展示的技能信息。
 * 包含技能的基本信息和状态。
 */
@Data
public class UserSkillVO {

    private Long id;

    private String name;

    private String displayName;

    private String description;

    private String version;

    private String author;

    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}