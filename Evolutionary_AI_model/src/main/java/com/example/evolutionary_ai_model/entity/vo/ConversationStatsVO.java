package com.example.evolutionary_ai_model.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话统计数据VO
 */
@Data
public class ConversationStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 总对话次数
    private Integer totalCount;

    // 今日对话次数
    private Integer todayCount;

    // 总消息数
    private Integer totalMessages;

    // 活跃对话数
    private Integer activeCount;
}