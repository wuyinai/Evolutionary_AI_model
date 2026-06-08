package com.example.evolutionary_ai_model.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用法：AI对话响应DTO，用于返回AI对话结果给前端。
 * 包含对话ID、消息ID、回复内容等信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    // 对话ID
    private String conversationId;

    // 消息ID
    private String messageId;

    // AI回复内容
    private String content;

    // 对话模式
    private String mode;

    // 时间戳
    private Long timestamp;
}