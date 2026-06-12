package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用法：AI模型配置实体类，存储用户自定义的AI模型推理参数配置。
 * 位于数据访问层，映射数据库表 ai_model_config。
 * 只包含推理参数（温度、token上限等），连接信息由关联的AiProviderConfig管理。
 * 一对多关系：一个AiProviderConfig可以关联多个AiModelConfig。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
@TableName("ai_model_config")
public class AiModelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 配置名称（用户自定义）
    private String configName;

    // 用户ID（配置所属用户）（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // 供应商配置ID，关联ai_provider_config.id（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long providerConfigId;

    // 模型名称（如：gpt-4o、qwen-turbo、deepseek-chat等）
    private String modelName;

    // 模型别名（用户自定义显示名称）
    private String modelAlias;

    // 模型类型：CHAT-对话模型 EMBEDDING-向量模型
    private String modelType;

    // 向量维度（仅向量模型使用）
    private Integer vectorDimensions;

    // 相似度阈值（仅向量模型使用，0.00-1.00）
    private BigDecimal similarityThreshold;

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

    // 是否默认模型：0-否 1-是
    private Integer isDefault;

    // 是否启用流式输出：0-否 1-是
    private Integer isStreamingEnabled;

    // 每日调用限额（次数），NULL表示无限制
    private Integer dailyQuota;

    // 每月调用限额（次数），NULL表示无限制
    private Integer monthlyQuota;

    // Token总量限额，NULL表示无限制（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tokenQuota;

    // 累计调用次数（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long usedCount;

    // 累计使用Token数（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
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