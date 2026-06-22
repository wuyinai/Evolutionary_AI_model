// AI对话相关API接口

import type { SendMessageRequest } from '@/types/conversation'

/**
 * 文档块信息接口
 */
export interface DocumentChunk {
  chunkId?: string
  content: string
  documentId?: number
  documentName?: string
  chunkIndex?: number
  similarityScore?: number
  summary?: string
}

/**
 * 使用fetch API实现流式对话
 * @param data 对话请求数据
 * @param onMessage 接收消息的回调函数
 * @param onError 发生错误的回调函数
 * @param onComplete 完成的回调函数
 * @param onDocumentChunks 接收文档块信息的回调函数（可选）
 */
export const streamChat = async (
  data: SendMessageRequest,
  onMessage: (chunk: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void,
  onDocumentChunks?: (chunks: DocumentChunk[]) => void,
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
    let isFirstEvent = true // 标记是否是第一个事件
    let eventCount = 0 // 事件计数器

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
          eventCount++
          const content = line.substring(5) // 去掉 "data:" 前缀
          if (content.trim()) {
            console.log(`事件 #${eventCount}:`, content.substring(0, 100))

            // 检查是否是第一个事件且可能是文档块信息
            if (isFirstEvent) {
              isFirstEvent = false
              console.log('这是第一个事件，尝试检测文档块信息')
              try {
                // 尝试解析为JSON数组（文档块信息）
                const parsed = JSON.parse(content)
                if (
                  Array.isArray(parsed) &&
                  parsed.length > 0 &&
                  parsed[0].chunkId &&
                  parsed[0].content &&
                  parsed[0].documentId
                ) {
                  // 这是文档块信息，调用回调并跳过
                  console.log('✓ 检测到文档块信息:', parsed.length, '个')
                  onDocumentChunks?.(parsed as DocumentChunk[])
                  continue // 跳过这个事件，不作为AI回复内容处理
                } else {
                  console.log('✗ 不是文档块信息，作为普通文本处理')
                }
              } catch (e) {
                // 解析失败，说明是普通文本内容，正常处理
                console.log('✗ JSON解析失败，作为普通文本处理:', e)
              }
            }
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