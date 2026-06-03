// 对话相关类型定义

export type MessageRole = 'user' | 'assistant'
export type ConversationMode = 'quick' | 'expert'

export interface Message {
  id: string
  role: MessageRole
  content: string
  timestamp: Date
}

export interface Conversation {
  id: string
  title: string
  mode: ConversationMode
  messages: Message[]
  createdAt: Date
  updatedAt: Date
}

export interface ConversationGroup {
  label: string // 例如："今天"、"昨天"、"7天内"
  conversations: Conversation[]
}

export interface SendMessageRequest {
  conversationId?: string
  message: string
  mode: ConversationMode
}

export interface SendMessageResponse {
  code: number
  message: string
  data: {
    messageId: string
    content: string
    timestamp: string
  }
}
