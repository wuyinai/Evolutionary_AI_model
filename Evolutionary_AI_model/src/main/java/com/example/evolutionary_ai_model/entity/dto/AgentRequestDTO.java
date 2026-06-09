package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用法：Agent任务请求DTO，用于接收前端发送的Agent任务请求。
 * 包含任务描述、模型配置ID、用户ID、可用工具列表等参数。
 * 位于数据传输层，封装Agent任务的请求参数，支持参数校验。
 */
@Data
public class AgentRequestDTO {

    // 任务描述（用户希望Agent完成的目标）
    @NotBlank(message = "任务描述不能为空")
    private String task;

    // 模型配置ID，指定使用的模型配置，可选（不传则使用用户默认模型）
    private Long configId;

    // 用户ID，用于获取用户的默认模型配置（后端从认证信息获取）
    private Long userId;

    // 可用工具列表，可选（不传则使用所有已注册工具）
    private List<String> availableTools;

    // 最大执行步数，防止无限循环（默认10步）
    private Integer maxSteps = 10;

    // 是否启用详细日志，可选（默认false）
    private Boolean enableDetailedLog = false;
}