package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：AI模型供应商实体类，存储AI服务供应商的基本信息。
 * 位于数据访问层，映射数据库表 ai_model_provider。
 * 包含供应商编码、名称、API端点、支持能力等配置信息。
 */
@Data
@TableName("ai_model_provider")
public class AiModelProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @TableId
    private Long id;

    // 供应商编码（如：OPENAI、AZURE_OPENAI、OLLAMA、QWEN、ERNIE、DEEPSEEK）
    private String providerCode;

    // 供应商名称（如：OpenAI、Azure OpenAI、Ollama、通义千问、文心一言、DeepSeek）
    private String providerName;

    // 供应商图标URL
    private String providerIcon;

    // 供应商描述
    private String description;

    // 默认API端点
    private String defaultEndpoint;

    // 是否支持流式输出：0-否 1-是
    private Integer supportsStreaming;

    // 是否支持视觉能力：0-否 1-是
    private Integer supportsVision;

    // 是否支持函数调用：0-否 1-是
    private Integer supportsFunctionCall;

    // 认证类型：API_KEY、BEARER_TOKEN、CUSTOM
    private String authType;

    // 配置模板（JSON格式，定义该供应商需要的配置项）
    private String configTemplate;

    // 状态：0-禁用 1-启用
    private Integer status;

    // 排序号
    private Integer sortOrder;

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