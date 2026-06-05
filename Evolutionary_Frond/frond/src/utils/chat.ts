// AI对话相关API接口

import { post, get } from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { SendMessageRequest, SendMessageResponse } from '@/types/conversation'

/**
 * 发送对话消息
 * @param data 对话请求数据
 */
export const sendMessage = (data: SendMessageRequest): Promise<ApiResponse<SendMessageResponse>> => {
  return post<SendMessageResponse>('/chat/send', data)
}

/**
 * 获取支持的对话模式列表
 */
export const getChatModes = (): Promise<ApiResponse<string[]>> => {
  return get<string[]>('/chat/modes')
}

/**
 * 使用fetch API实现流式对话
 * @param data 对话请求数据
 * @param onMessage 接收消息的回调函数
 * @param onError 发生错误的回调函数
 * @param onComplete 完成的回调函数
 */
export const streamChat = async (
  data: SendMessageRequest,
  onMessage: (chunk: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void,
): Promise<void> => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8234'
  const token = localStorage.getItem('token')

  try {
    const response = await fetch(`${baseUrl}/chat/stream`, {
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