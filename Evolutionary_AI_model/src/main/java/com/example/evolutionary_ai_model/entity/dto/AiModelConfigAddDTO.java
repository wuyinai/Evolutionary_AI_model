package com.example.evolutionary_ai_model.entity.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用法：添加模型配置请求DTO，用于接收前端添加模型配置的请求。
 * 包含供应商配置ID、配置名称、模型名称等参数。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
public class AiModelConfigAddDTO {

    // 配置名称（用户自定义）
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    // 供应商配置ID（关联ai_provider_config.id）（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "供应商配置ID不能为空")
    private Long providerConfigId;

    // 模型名称（如：deepseek-chat、gpt-4o等）
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    // 模型别名（用户自定义显示名称）
    private String modelAlias;

    // 模型类型：CHAT-对话模型 EMBEDDING-向量模型（默认为CHAT）
    private String modelType;

    // 向量维度（仅向量模型使用）
    private Integer vectorDimensions;

    // 相似度阈值（仅向量模型使用，0.00-1.00）
    private BigDecimal similarityThreshold;

    // 温度参数（0.00-2.00）
    private BigDecimal temperature;

    // 最大输出Token数
    private Integer maxTokens;

    // Top-P采样参数
    private BigDecimal topP;

    // 频率惩罚参数
    private BigDecimal frequencyPenalty;

    // 存在惩罚参数
    private BigDecimal presencePenalty;

    // 是否默认模型
    private Integer isDefault;

    // 是否启用流式输出
    private Integer isStreamingEnabled;

    // 备注
    private String remark;
}