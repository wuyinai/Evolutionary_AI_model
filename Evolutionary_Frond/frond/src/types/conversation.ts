// 对话相关类型定义

export type MessageRole = 'user' | 'assistant'
export type ConversationMode = 'quick' | 'expert'

export interface Message {
  id: string
  role: MessageRole
  content: string
  timestamp: Date
  isStreaming?: boolean // 是否正在流式输出
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

// 用于API请求的消息格式
export interface ChatMessageDTO {
  role: string
  content: string
}

export interface SendMessageRequest {
  conversationId?: string
  message: string
  mode: ConversationMode
  history?: ChatMessageDTO[]
}

export interface SendMessageResponse {
  conversationId: string
  messageId: string
  content: string
  mode: string
  timestamp: number
}