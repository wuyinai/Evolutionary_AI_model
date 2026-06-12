// AI模型配置相关类型定义

// 模型类型枚举
export enum ModelType {
  CHAT = 'CHAT',        // 对话模型
  EMBEDDING = 'EMBEDDING' // 向量模型
}

// 模型配置VO
export interface AiModelConfigVO {
  id: string // ID使用string类型，避免JS精度丢失
  configName: string
  providerConfigId: string // 供应商配置ID
  providerName: string
  modelName: string
  modelAlias: string | null
  modelType: string // 模型类型：CHAT-对话模型 EMBEDDING-向量模型
  vectorDimensions: number | null // 向量维度（仅向量模型使用）
  similarityThreshold: number | null // 相似度阈值（仅向量模型使用）
  temperature: number
  maxTokens: number | null
  topP?: number
  frequencyPenalty?: number
  presencePenalty?: number
  isDefault: number
  isStreamingEnabled: number
  status: number
  usedCount: number
  lastUsedTime: string | null
  createTime: string
  remark: string | null
}

// 模型供应商VO
export interface AiModelProviderVO {
  id: string // ID使用string类型，避免JS精度丢失
  providerCode: string
  providerName: string
  providerIcon: string | null
  description: string | null
  defaultEndpoint: string
  supportsStreaming: number
  supportsVision: number
  supportsFunctionCall: number
  authType: string
  configTemplate: string | null
  status: number
  sortOrder: number
  createTime: string
}

// 添加模型配置请求
export interface AiModelConfigAddDTO {
  configName: string
  providerConfigId: string // 关联供应商配置ID
  modelName: string
  modelAlias?: string
  modelType?: string // 模型类型：CHAT-对话模型 EMBEDDING-向量模型（默认为CHAT）
  vectorDimensions?: number // 向量维度（仅向量模型使用）
  similarityThreshold?: number // 相似度阈值（仅向量模型使用）
  temperature?: number
  maxTokens?: number
  topP?: number
  frequencyPenalty?: number
  presencePenalty?: number
  isDefault?: number
  isStreamingEnabled?: number
  remark?: string
}

// 模型配置列表响应
export interface ModelConfigListResponse {
  code: number
  message: string
  data: AiModelConfigVO[]
}

// 供应商列表响应
export interface ProviderListResponse {
  code: number
  message: string
  data: AiModelProviderVO[]
}

// 添加模型配置响应
export interface AddModelConfigResponse {
  code: number
  message: string
  data: string // 返回配置ID（string类型）
}

// 测试连接响应
export interface TestConnectionResponse {
  code: number
  message: string
  data: string
}