package com.example.evolutionary_ai_model.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用法：会话消息VO类，用于返回对话历史记录给前端。
 * 位于数据访问层，作为视图对象封装会话消息数据。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
public class ConversationMessageVO {

    // 消息ID
    private String messageId;

    // 会话ID
    private String conversationId;

    // 角色：USER-用户、ASSISTANT-助手、SYSTEM-系统
    private String role;

    // 消息内容
    private String content;

    // 知识库文档块信息（JSON格式）
    private String documentChunks;

    // Token数
    private Integer tokens;

    // 父消息ID（用于消息树结构）
    private String parentMessageId;

    // 模型配置ID，关联ai_model_config.id（记录该消息使用的模型）（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long configId;

    // 关联的日志ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long logId;

    // 创建时间
    private LocalDateTime createTime;
}