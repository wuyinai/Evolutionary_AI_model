package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：AI供应商配置实体类，存储用户配置的供应商连接信息。
 * 位于数据访问层，映射数据库表 ai_provider_config。
 * 管理API密钥、端点地址、协议类型等连接配置，一对多关联ai_model_config。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
@TableName("ai_provider_config")
public class AiProviderConfig implements Serializable {

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

    // 供应商ID，关联ai_model_provider.id（序列化为String）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long providerId;

    // 供应商编码（冗余字段，便于查询）
    private String providerCode;

    // 协议类型（冗余字段，便于查询）
    private String protocolType;

    // API密钥（AES加密存储）
    private String apiKey;

    // API端点地址（覆盖默认端点）
    private String apiEndpoint;

    // 扩展配置（JSON格式，如：deploymentName、secretKey、accessToken等）
    private String extraConfig;

    // 是否默认配置：0-否 1-是
    private Integer isDefault;

    // 请求超时时间（秒）
    private Integer timeoutSeconds;

    // 最大重试次数
    private Integer maxRetries;

    // 状态：0-禁用 1-启用
    private Integer status;

    // 创建者
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新者
    @TableField(fill = FieldFill.INSERT_UPDATE)
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