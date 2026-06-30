package com.example.evolutionary_ai_model.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人主页统计数据VO
 */
@Data
public class ProfileStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 对话统计
    private ConversationStatsVO conversationStats;

    // Token统计
    private TokenStatsVO tokenStats;
}