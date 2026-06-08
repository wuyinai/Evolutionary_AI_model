package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用法：更新模型配置请求DTO，用于接收前端更新模型配置的请求。
 * 包含配置ID和可更新的参数。
 */
@Data
public class AiModelConfigUpdateDTO {

    // 配置ID
    @NotNull(message = "配置ID不能为空")
    private Long id;

    // 配置名称（用户自定义）
    private String configName;

    // 模型名称
    private String modelName;

    // 模型别名
    private String modelAlias;

    // API密钥（可选更新）
    private String apiKey;

    // API端点地址
    private String apiEndpoint;

    // 扩展配置
    private String extraConfig;

    // 温度参数
    private BigDecimal temperature;

    // 最大输出Token数
    private Integer maxTokens;

    // Top-P采样参数
    private BigDecimal topP;

    // 频率惩罚参数
    private BigDecimal frequencyPenalty;

    // 存在惩罚参数
    private BigDecimal presencePenalty;

    // 请求超时时间
    private Integer timeoutSeconds;

    // 最大重试次数
    private Integer maxRetries;

    // 是否默认模型
    private Integer isDefault;

    // 是否启用流式输出
    private Integer isStreamingEnabled;

    // 状态
    private Integer status;

    // 备注
    private String remark;
}