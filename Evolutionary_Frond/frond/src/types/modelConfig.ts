// AI模型配置相关类型定义

// 模型配置VO
export interface AiModelConfigVO {
  id: string // ID使用string类型，避免JS精度丢失
  configName: string
  providerId: string
  providerCode: string
  providerName: string
  modelName: string
  modelAlias: string | null
  apiKeyMasked: string
  apiEndpoint: string
  temperature: number
  maxTokens: number | null
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
export interface AiModelConfigAddForm {
  configName: string
  providerCode: string
  modelName: string
  modelAlias?: string
  apiKey: string
  apiEndpoint?: string
  temperature?: number
  maxTokens?: number
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