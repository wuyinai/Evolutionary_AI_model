package com.example.evolutionary_ai_model.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用法：供应商返回VO，用于返回供应商信息给前端。
 * 包含供应商编码、名称、图标、能力描述等。
 * ID字段使用ToStringSerializer序列化，避免JavaScript精度丢失。
 */
@Data
public class AiModelProviderVO {

    // 供应商ID（序列化为String，避免JS精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    // 供应商编码
    private String providerCode;

    // 供应商名称
    private String providerName;

    // 供应商图标URL
    private String providerIcon;

    // 供应商描述
    private String description;

    // 默认API端点
    private String defaultEndpoint;

    // 是否支持流式输出
    private Integer supportsStreaming;

    // 是否支持视觉能力
    private Integer supportsVision;

    // 是否支持函数调用
    private Integer supportsFunctionCall;

    // 认证类型
    private String authType;

    // 配置模板（JSON格式）
    private String configTemplate;

    // 状态
    private Integer status;

    // 排序号
    private Integer sortOrder;

    // 创建时间
    private LocalDateTime createTime;
}