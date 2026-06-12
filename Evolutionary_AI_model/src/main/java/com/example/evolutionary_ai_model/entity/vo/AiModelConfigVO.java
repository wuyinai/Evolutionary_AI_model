package com.example.evolutionary_ai_model.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用法：模型配置返回VO，用于返回模型配置信息给前端。
 * API密钥字段已脱敏，不返回完整密钥。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
public class AiModelConfigVO {

    // 配置ID（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    // 配置名称
    private String configName;

    // 供应商配置ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long providerConfigId;

    // 供应商名称
    private String providerName;

    // 模型名称
    private String modelName;

    // 模型别名
    private String modelAlias;

    // 模型类型：CHAT-对话模型 EMBEDDING-向量模型
    private String modelType;

    // 向量维度（仅向量模型使用）
    private Integer vectorDimensions;

    // 相似度阈值（仅向量模型使用）
    private BigDecimal similarityThreshold;

    // 温度参数
    private BigDecimal temperature;

    // 最大输出Token数
    private Integer maxTokens;

    // 是否默认模型
    private Integer isDefault;

    // 是否启用流式输出
    private Integer isStreamingEnabled;

    // 状态
    private Integer status;

    // 累计调用次数
    private Long usedCount;

    // 最后使用时间
    private LocalDateTime lastUsedTime;

    // 创建时间
    private LocalDateTime createTime;

    // 备注
    private String remark;
}