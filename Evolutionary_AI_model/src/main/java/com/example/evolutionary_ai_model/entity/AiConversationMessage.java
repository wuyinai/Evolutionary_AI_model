package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：AI会话消息实体类，存储对话中的每条消息记录。
 * 位于数据访问层，映射数据库表 ai_conversation_message。
 * 包含消息角色、内容、Token数、父消息ID等信息，支持消息树结构。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 * 支持逻辑删除功能，通过del_flag字段标记删除状态。
 */
@Data
@TableName("ai_conversation_message")
public class AiConversationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 消息ID
    private String messageId;

    // 会话ID
    private String conversationId;

    // 角色：USER-用户、ASSISTANT-助手、SYSTEM-系统
    private String role;

    // 消息内容
    private String content;

    // Token数
    private Integer tokens;

    // 父消息ID（用于消息树结构）
    private String parentMessageId;

    // 关联的日志ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long logId;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 删除标志：0-未删除 1-已删除
    @TableLogic
    private Integer delFlag;
}