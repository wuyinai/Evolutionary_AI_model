package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用法：添加模型配置请求DTO，用于接收前端添加模型配置的请求。
 * 包含供应商编码、配置名称、模型名称、API密钥等参数。
 */
@Data
public class AiModelConfigAddDTO {

    // 配置名称（用户自定义）
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    // 供应商编码（如：DEEPSEEK、OPENAI、QWEN等）
    @NotBlank(message = "供应商编码不能为空")
    private String providerCode;

    // 模型名称（如：deepseek-chat、gpt-4o等）
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    // 模型别名（用户自定义显示名称）
    private String modelAlias;

    // API密钥
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    // API端点地址（可选，覆盖默认端点）
    private String apiEndpoint;

    // 扩展配置（JSON格式）
    private String extraConfig;

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

    // 请求超时时间（秒）
    private Integer timeoutSeconds;

    // 最大重试次数
    private Integer maxRetries;

    // 是否默认模型
    private Integer isDefault;

    // 是否启用流式输出
    private Integer isStreamingEnabled;

    // 备注
    private String remark;
}