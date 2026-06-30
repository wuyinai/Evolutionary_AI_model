package com.example.evolutionary_ai_model.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Token统计数据VO
 */
@Data
public class TokenStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 总Token消耗
    private Long totalTokens;

    // 输入Token数
    private Long inputTokens;

    // 输出Token数
    private Long outputTokens;

    // 今日Token消耗
    private Long todayTokens;

    // 近7天Token使用趋势
    private List<DailyTokenUsageVO> weeklyTrend;
}