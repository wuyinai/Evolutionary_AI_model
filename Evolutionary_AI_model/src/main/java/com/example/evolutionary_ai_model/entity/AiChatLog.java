package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用法：AI调用日志实体类，记录每次AI模型调用的详细信息。
 * 位于数据访问层，映射数据库表 ai_chat_log。
 * 包含请求内容、响应内容、Token统计、耗时、费用等信息，用于审计和分析。
 */
@Data
@TableName("ai_chat_log")
public class AiChatLog implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @TableId
    private Long id;

    // 追踪ID（用于链路追踪）
    private String traceId;

    // 模型配置ID，关联ai_model_config.id
    private Long configId;

    // 供应商编码
    private String providerCode;

    // 使用的模型名称
    private String modelName;

    // 调用用户ID
    private Long userId;

    // 调用用户名
    private String username;

    // 会话ID（同一对话多轮）
    private String sessionId;

    // 对话ID
    private String conversationId;

    // 请求类型：CHAT-对话、COMPLETION-补全、EMBEDDING-向量化
    private String requestType;

    // 请求内容（用户输入）
    private String requestContent;

    // 请求Token数
    private Integer requestTokens;

    // 响应内容（AI输出）
    private String responseContent;

    // 响应Token数
    private Integer responseTokens;

    // 总Token数
    private Integer totalTokens;

    // 是否流式请求：0-否 1-是
    private Integer isStreaming;

    // 响应状态：SUCCESS-成功、FAILED-失败、TIMEOUT-超时
    private String responseStatus;

    // 错误码
    private String errorCode;

    // 错误信息
    private String errorMessage;

    // 响应耗时（毫秒）
    private Long latencyMs;

    // 首Token延迟（毫秒，流式场景）
    private Integer firstTokenLatencyMs;

    // 费用金额（美元）
    private BigDecimal costAmount;

    // 输入Token单价（美元/千Token）
    private BigDecimal inputUnitPrice;

    // 输出Token单价（美元/千Token）
    private BigDecimal outputUnitPrice;

    // 客户端IP
    private String clientIp;

    // 用户代理
    private String userAgent;

    // 请求时间
    private LocalDateTime requestTime;

    // 响应时间
    private LocalDateTime responseTime;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}