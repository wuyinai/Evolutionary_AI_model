package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用法：AI对话请求DTO，用于接收前端发送的对话请求。
 * 包含对话ID、消息内容、对话模式、用户ID、模型配置ID等参数。
 */
@Data
public class ChatRequestDTO {
    // 对话ID，首次对话时可为空
    private String conversationId;

    // 用户发送的消息内容
    @NotBlank(message = "消息内容不能为空")
    private String message;

    // 对话模式：quick-快速模式，expert-专家模式
    @NotBlank(message = "对话模式不能为空")
    private String mode;

    // 历史消息列表，用于多轮对话上下文
    private List<ChatMessageDTO> history;

    // 模型配置ID，指定使用的模型配置，可选（不传则使用用户默认模型）
    private Long configId;

    // 用户ID，用于获取用户的默认模型配置（后端从认证信息获取）
    private Long userId;
}