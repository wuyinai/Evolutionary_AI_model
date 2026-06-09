// Agent相关类型定义

/**
 * Agent请求类型
 */
export interface AgentRequest {
  task: string // 任务描述
  configId?: number // 模型配置ID
  availableTools?: string[] // 可用工具列表
  maxSteps?: number // 最大执行步骤
}

/**
 * 执行日志步骤类型
 */
export interface ExecutionLogStep {
  step: number // 步骤编号
  thought: string // 思考内容
  action: string // 执行动作
  actionInput: string // 动作输入
  observation: string // 观察结果
}

/**
 * Agent结果类型
 */
export interface AgentResult {
  taskId: string // 任务ID
  task: string // 任务描述
  result: string // 执行结果
  executionLogs: ExecutionLogStep[] // 执行日志
  status: 'RUNNING' | 'COMPLETED' | 'FAILED' | 'TIMEOUT' // 执行状态
  startTime: string // 开始时间
  endTime?: string // 结束时间
  duration?: number // 执行时长（毫秒）
  error?: string // 错误信息
}

/**
 * 工具信息类型
 */
export interface ToolInfo {
  name: string // 工具名称
  description: string // 工具描述
  parameters: ToolParameter[] // 工具参数
}

/**
 * 工具参数类型
 */
export interface ToolParameter {
  name: string // 参数名称
  type: string // 参数类型
  description: string // 参数描述
  required: boolean // 是否必填
}

/**
 * Agent执行状态枚举
 */
export enum AgentExecutionStatus {
  IDLE = 'IDLE', // 空闲
  RUNNING = 'RUNNING', // 执行中
  COMPLETED = 'COMPLETED', // 已完成
  FAILED = 'FAILED', // 失败
}

/**
 * Agent Store状态类型
 */
export interface AgentState {
  currentTask: string // 当前任务
  selectedTools: string[] // 选中的工具
  configId: number | null // 模型配置ID
  maxSteps: number // 最大执行步骤

  executionStatus: AgentExecutionStatus // 执行状态
  executionResult: AgentResult | null // 执行结果
  streamingContent: string // 流式输出内容

  availableTools: ToolInfo[] // 可用工具列表
  loadingTools: boolean // 是否正在加载工具

  error: string | null // 错误信息
}