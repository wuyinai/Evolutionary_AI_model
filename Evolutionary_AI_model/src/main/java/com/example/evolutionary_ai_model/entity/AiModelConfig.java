package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用法：AI模型配置实体类，存储用户自定义的AI模型配置信息。
 * 位于数据访问层，映射数据库表 ai_model_config。
 * 包含API密钥、模型名称、温度参数、配额限制等配置信息。
 */
@Data
@TableName("ai_model_config")
public class AiModelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @TableId
    private Long id;

    // 配置名称（用户自定义）
    private String configName;

    // 用户ID（配置所属用户）
    private Long userId;

    // 供应商ID，关联ai_model_provider.id
    private Long providerId;

    // 供应商编码（冗余字段，便于查询）
    private String providerCode;

    // 模型名称（如：gpt-4o、qwen-turbo、deepseek-chat等）
    private String modelName;

    // 模型别名（用户自定义显示名称）
    private String modelAlias;

    // API密钥（AES加密存储）
    private String apiKey;

    // API端点地址（覆盖默认端点）
    private String apiEndpoint;

    // 扩展配置（JSON格式，如：deploymentName、secretKey等）
    private String extraConfig;

    // 温度参数（0.00-2.00），控制输出随机性
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

    // 是否默认模型：0-否 1-是
    private Integer isDefault;

    // 是否启用流式输出：0-否 1-是
    private Integer isStreamingEnabled;

    // 每日调用限额（次数），NULL表示无限制
    private Integer dailyQuota;

    // 每月调用限额（次数），NULL表示无限制
    private Integer monthlyQuota;

    // Token总量限额，NULL表示无限制
    private Long tokenQuota;

    // 累计调用次数
    private Long usedCount;

    // 累计使用Token数
    private Long usedTokens;

    // 最后使用时间
    private LocalDateTime lastUsedTime;

    // 状态：0-禁用 1-启用
    private Integer status;

    // 创建者
    private String createBy;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新者
    private String updateBy;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除标志：0-未删除 1-已删除
    @TableLogic
    private Integer delFlag;

    // 备注
    private String remark;
}