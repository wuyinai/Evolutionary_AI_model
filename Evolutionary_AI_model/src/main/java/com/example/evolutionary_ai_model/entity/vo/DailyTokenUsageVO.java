package com.example.evolutionary_ai_model.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 每日Token使用数据VO
 */
@Data
public class DailyTokenUsageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 日期（周一、周二等）
    private String date;

    // Token使用量
    private Long tokens;
}