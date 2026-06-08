package com.example.evolutionary_ai_model.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用法：AI供应商配置VO，用于返回给前端的供应商配置信息。
 * 不包含敏感信息（如完整的API密钥），只返回脱敏后的密钥。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
public class AiProviderConfigVO {

    // 配置ID（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    // 配置名称（用户自定义）
    private String configName;

    // 供应商ID（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long providerId;

    // 供应商编码
    private String providerCode;

    // 供应商名称
    private String providerName;

    // 协议类型
    private String protocolType;

    // API密钥（脱敏显示，如：sk-***xxx）
    private String apiKeyMasked;

    // API端点地址
    private String apiEndpoint;

    // 是否默认配置：0-否 1-是
    private Integer isDefault;

    // 状态：0-禁用 1-启用
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 备注
    private String remark;
}