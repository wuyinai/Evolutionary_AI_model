package com.example.evolutionary_ai_model.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用法：对话消息DTO，用于表示单条对话消息。
 * 包含消息角色（用户/AI）和消息内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    // 消息角色：user-用户消息，assistant-AI回复
    private String role;

    // 消息内容
    private String content;
}