// Agent相关API接口

import type { AgentRequest, AgentResult, ToolInfo } from '@/types/agent'
import { get, post } from './request'

/**
 * 流式执行Agent任务
 * @param data Agent请求数据
 * @param onMessage 接收消息的回调函数
 * @param onError 发生错误的回调函数
 * @param onComplete 完成的回调函数
 */
export const executeAgentTask = async (
  data: AgentRequest,
  onMessage: (chunk: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void,
): Promise<void> => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8234'
  const token = localStorage.getItem('token')

  try {
    const response = await fetch(`${baseUrl}/chat/agent/task`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        Accept: 'text/event-stream',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('Response body is null')
    }

    const decoder = new TextDecoder()
    let buffer = '' // 用于存储不完整的数据

    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        onComplete?.()
        break
      }

      // 解码当前数据块
      buffer += decoder.decode(value, { stream: true })

      // 按行分割处理SSE数据
      const lines = buffer.split('\n')
      buffer = '' // 清空缓冲区

      // 处理每一行
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i]

        // 如果是最后一行且不完整，保存到缓冲区等待下次处理
        if (i === lines.length - 1 && !line.endsWith('\n') && line.length > 0) {
          buffer = line
          continue
        }

        // 解析SSE格式: "data:内容"
        if (line.startsWith('data:')) {
          const content = line.substring(5) // 去掉 "data:" 前缀
          if (content.trim()) {
            onMessage(content)
          }
        }
        // 也处理没有前缀的纯文本格式（兼容）
        else if (line.trim() && !line.startsWith(':')) {
          onMessage(line)
        }
      }
    }
  } catch (error) {
    onError?.(error instanceof Error ? error : new Error('Unknown error'))
  }
}

/**
 * 同步执行Agent任务
 * @param data Agent请求数据
 * @returns Agent执行结果
 */
export const executeAgentTaskSync = async (data: AgentRequest): Promise<AgentResult> => {
  const response = await post<AgentResult>('/chat/agent/task/sync', data)
  return response.data
}

/**
 * 获取可用工具列表
 * @returns 工具列表
 */
export const getAvailableTools = async (): Promise<ToolInfo[]> => {
  const response = await get<string[]>('/chat/agent/tools')
  const toolNames = response.data || []

  // 将字符串数组转换为ToolInfo对象数组
  const toolDescriptions: Record<string, string> = {
    calculator: '计算器工具，支持基本数学运算（加、减、乘、除）',
    search: '搜索工具，根据关键词搜索相关信息',
    weather: '天气查询工具，查询指定城市的天气信息',
    time: '时间查询工具，查询当前时间和日期信息',
  }

  return toolNames.map((name) => ({
    name,
    description: toolDescriptions[name] || `${name}工具`,
    parameters: [],
  }))
}