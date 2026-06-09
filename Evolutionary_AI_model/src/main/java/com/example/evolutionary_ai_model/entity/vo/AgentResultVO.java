package com.example.evolutionary_ai_model.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用法：Agent任务结果VO，用于返回Agent任务的执行结果。
 * 包含任务ID、执行状态、最终答案、工具执行日志等信息。
 * 位于视图层，封装Agent任务的执行结果，供前端展示。
 */
@Data
public class AgentResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 任务ID（唯一标识）
    private String taskId;

    // 执行状态：SUCCESS-成功、FAILED-失败、TIMEOUT-超时
    private String status;

    // 最终答案（Agent的最终输出）
    private String finalAnswer;

    // 工具执行日志列表
    private List<ToolExecutionLog> toolLogs;

    // 总执行步数
    private Integer totalSteps;

    // 总耗时（毫秒）
    private Long totalTimeMs;

    // 任务开始时间
    private LocalDateTime startTime;

    // 任务结束时间
    private LocalDateTime endTime;

    // 错误信息（如果失败）
    private String errorMessage;

    /**
     * 用法：工具执行日志，记录单个工具的执行过程。
     * 包含工具名称、执行参数、执行结果、执行时间等信息。
     */
    @Data
    public static class ToolExecutionLog implements Serializable {

        private static final long serialVersionUID = 1L;

        // 执行步骤序号
        private Integer stepNumber;

        // 工具名称
        private String toolName;

        // 工具描述
        private String toolDescription;

        // 执行参数（JSON格式）
        private String parameters;

        // 执行结果
        private String result;

        // 执行状态：SUCCESS-成功、FAILED-失败
        private String executionStatus;

        // 执行耗时（毫秒）
        private Long executionTimeMs;

        // 执行时间
        private LocalDateTime executionTime;

        // 错误信息（如果失败）
        private String errorMessage;
    }
}