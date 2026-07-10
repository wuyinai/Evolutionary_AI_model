// 对话相关类型定义

export type MessageRole = 'user' | 'assistant'

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

export interface Message {
  id: string
  role: MessageRole
  content: string
  timestamp: Date
  isStreaming?: boolean // 是否正在流式输出
  documentChunks?: DocumentChunk[] // 相关的文档块信息
}

export interface Conversation {
  id: string
  title: string
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
  configId?: string // 模型配置ID（使用string类型）
  userId?: number // 用户ID（可选，后端从认证信息获取）
  history?: ChatMessageDTO[]
  ragTopK?: number // RAG检索数量
  roleId?: string // AI角色ID，用于加载角色系统提示词（可选）
}

// 会话管理API请求DTO
export interface CreateConversationDTO {
  title?: string
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

// ============ 后端API返回数据类型 ============

/**
 * 后端返回的会话实体类型（对应 AiConversation）
 * ID字段使用string类型，避免JavaScript精度丢失
 */
export interface ApiConversation {
  id: string // 主键ID
  conversationId: string // 会话ID
  userId: string // 用户ID
  configId?: string // 模型配置ID
  pinnedConfigId?: string // 钉选的模型配置ID
  title: string // 会话标题
  systemPrompt?: string // 系统提示词
  messageCount?: number // 消息数量
  totalTokens?: string // 累计Token数
  totalCost?: number // 累计费用（美元）
  lastMessageTime?: string // 最后消息时间
  status?: number // 状态：0-已归档 1-活跃
  createTime: string // 创建时间
  updateTime: string // 更新时间
  delFlag?: number // 删除标志
}

/**
 * 后端返回的会话消息VO类型（对应 ConversationMessageVO）
 */
export interface ApiConversationMessage {
  messageId: string // 消息ID
  conversationId: string // 会话ID
  role: string // 角色：USER-用户、ASSISTANT-助手、SYSTEM-系统
  content: string // 消息内容
  documentChunks?: string // 知识库文档块信息（JSON字符串）
  tokens?: number // Token数
  parentMessageId?: string // 父消息ID
  logId?: string // 关联的日志ID
  createTime: string // 创建时间
}