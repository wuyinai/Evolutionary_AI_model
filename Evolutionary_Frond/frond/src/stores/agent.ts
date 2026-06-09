// Agent状态管理

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { executeAgentTask, executeAgentTaskSync, getAvailableTools } from '@/utils/agent'
import type { AgentRequest, AgentResult, ToolInfo, AgentExecutionStatus } from '@/types/agent'

export const useAgentStore = defineStore('agent', () => {
  // 状态
  const currentTask = ref<string>('')
  const selectedTools = ref<string[]>([])
  const configId = ref<number | null>(null)
  const maxSteps = ref<number>(5)

  const executionStatus = ref<AgentExecutionStatus>('IDLE')
  const executionResult = ref<AgentResult | null>(null)
  const streamingContent = ref<string>('')

  const availableTools = ref<ToolInfo[]>([])
  const loadingTools = ref<boolean>(false)

  const error = ref<string | null>(null)

  // 计算属性
  const isExecuting = computed(() => executionStatus.value === 'RUNNING')
  const hasResult = computed(() => executionResult.value !== null)
  const hasError = computed(() => error.value !== null)

  /**
   * 加载可用工具列表
   */
  const loadAvailableTools = async () => {
    loadingTools.value = true
    error.value = null

    try {
      const tools = await getAvailableTools()
      availableTools.value = tools
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载工具列表失败'
      console.error('加载工具列表失败:', err)
    } finally {
      loadingTools.value = false
    }
  }

  /**
   * 选择/取消选择工具
   */
  const toggleTool = (toolName: string) => {
    const index = selectedTools.value.indexOf(toolName)
    if (index > -1) {
      selectedTools.value.splice(index, 1)
    } else {
      selectedTools.value.push(toolName)
    }
  }

  /**
   * 设置模型配置ID
   */
  const setConfigId = (id: number | null) => {
    configId.value = id
  }

  /**
   * 设置当前任务
   */
  const setCurrentTask = (task: string) => {
    currentTask.value = task
  }

  /**
   * 设置最大执行步骤
   */
  const setMaxSteps = (steps: number) => {
    maxSteps.value = steps
  }

  /**
   * 流式执行Agent任务
   */
  const executeStreaming = async () => {
    if (!currentTask.value.trim()) {
      error.value = '请输入任务描述'
      return
    }

    executionStatus.value = 'RUNNING'
    streamingContent.value = ''
    executionResult.value = null
    error.value = null

    const request: AgentRequest = {
      task: currentTask.value,
      configId: configId.value || undefined,
      availableTools: selectedTools.value.length > 0 ? selectedTools.value : undefined,
      maxSteps: maxSteps.value,
    }

    try {
      await executeAgentTask(
        request,
        (chunk: string) => {
          // 流式输出内容
          streamingContent.value += chunk
        },
        (err: Error) => {
          executionStatus.value = 'FAILED'
          error.value = err.message
          console.error('Agent执行失败:', err)
        },
        () => {
          // 执行完成
          executionStatus.value = 'COMPLETED'
        },
      )
    } catch (err) {
      executionStatus.value = 'FAILED'
      error.value = err instanceof Error ? err.message : '执行失败'
      console.error('Agent执行失败:', err)
    }
  }

  /**
   * 同步执行Agent任务
   */
  const executeSync = async () => {
    if (!currentTask.value.trim()) {
      error.value = '请输入任务描述'
      return
    }

    executionStatus.value = 'RUNNING'
    executionResult.value = null
    error.value = null

    const request: AgentRequest = {
      task: currentTask.value,
      configId: configId.value || undefined,
      availableTools: selectedTools.value.length > 0 ? selectedTools.value : undefined,
      maxSteps: maxSteps.value,
    }

    try {
      const result = await executeAgentTaskSync(request)
      executionResult.value = result
      executionStatus.value = 'COMPLETED'
    } catch (err) {
      executionStatus.value = 'FAILED'
      error.value = err instanceof Error ? err.message : '执行失败'
      console.error('Agent执行失败:', err)
    }
  }

  /**
   * 清空结果
   */
  const clearResult = () => {
    executionResult.value = null
    streamingContent.value = ''
    executionStatus.value = 'IDLE'
    error.value = null
  }

  /**
   * 重置状态
   */
  const reset = () => {
    currentTask.value = ''
    selectedTools.value = []
    configId.value = null
    maxSteps.value = 5
    executionStatus.value = 'IDLE'
    executionResult.value = null
    streamingContent.value = ''
    error.value = null
  }

  return {
    // 状态
    currentTask,
    selectedTools,
    configId,
    maxSteps,
    executionStatus,
    executionResult,
    streamingContent,
    availableTools,
    loadingTools,
    error,

    // 计算属性
    isExecuting,
    hasResult,
    hasError,

    // 方法
    loadAvailableTools,
    toggleTool,
    setConfigId,
    setCurrentTask,
    setMaxSteps,
    executeStreaming,
    executeSync,
    clearResult,
    reset,
  }
})