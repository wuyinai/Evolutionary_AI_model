package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用法：AI会话实体类，存储用户的AI对话会话信息。
 * 位于数据访问层，映射数据库表 ai_conversation。
 * 包含会话标题、系统提示词、消息统计、累计费用等信息。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
@TableName("ai_conversation")
public class AiConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 会话ID
    private String conversationId;

    // 用户ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // 模型配置ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long configId;

    // 钉选的模型配置ID，关联ai_model_config.id（用户在聊天界面选择的模型）（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pinnedConfigId;

    // 会话标题（自动生成或用户自定义）
    private String title;

    // 系统提示词
    private String systemPrompt;

    // 消息数量
    private Integer messageCount;

    // 累计Token数（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long totalTokens;

    // 累计费用（美元）
    private BigDecimal totalCost;

    // 最后消息时间
    private LocalDateTime lastMessageTime;

    // 状态：0-已归档 1-活跃
    private Integer status;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除标志：0-未删除 1-已删除
    @TableLogic
    private Integer delFlag;
}