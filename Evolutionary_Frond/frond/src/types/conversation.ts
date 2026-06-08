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
  pinnedConfigId?: string // 钉选的模型配置ID（新功能）
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
  configId?: string // 模型配置ID（使用string类型）
  userId?: number // 用户ID（可选，后端从认证信息获取）
  history?: ChatMessageDTO[]
}

export interface SendMessageResponse {
  conversationId: string
  messageId: string
  content: string
  mode: string
  timestamp: number
}

// 会话管理API请求DTO
export interface CreateConversationDTO {
  title?: string
  mode?: ConversationMode
  pinnedConfigId?: string // 钉选的模型配置ID
}

export interface UpdateConversationTitleDTO {
  conversationId: string
  title: string
}

export interface PinModelToConversationDTO {
  conversationId: string
  configId: string // 模型配置ID
}