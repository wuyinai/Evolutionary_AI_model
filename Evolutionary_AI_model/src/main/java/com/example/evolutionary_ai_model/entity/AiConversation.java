package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用法：AI会话实体类，存储用户的AI对话会话信息。
 * 位于数据访问层，映射数据库表 ai_conversation。
 * 包含会话标题、系统提示词、消息统计、累计费用等信息。
 */
@Data
@TableName("ai_conversation")
public class AiConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @TableId
    private Long id;

    // 会话ID
    private String conversationId;

    // 用户ID
    private Long userId;

    // 模型配置ID
    private Long configId;

    // 会话标题（自动生成或用户自定义）
    private String title;

    // 系统提示词
    private String systemPrompt;

    // 消息数量
    private Integer messageCount;

    // 累计Token数
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