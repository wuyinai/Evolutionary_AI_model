// AI供应商配置相关类型定义

// 协议类型枚举
export type ProtocolType = 'OPENAI' | 'ANTHROPIC' | 'OLLAMA' | 'AZURE_OPENAI' | 'QWEN' | 'ERNIE' | 'DEEPSEEK' | 'CUSTOM'

// 供应商配置VO（返回给前端）
export interface AiProviderConfigVO {
  id: string // ID使用string类型，避免JS精度丢失
  configName: string
  providerId: string
  providerCode: string
  providerName: string
  protocolType: ProtocolType
  apiKeyMasked: string
  apiEndpoint: string
  extraConfig?: string
  isDefault: number
  timeoutSeconds?: number
  maxRetries?: number
  status: number
  createTime: string
  updateTime: string
  remark?: string
}

// 供应商配置添加请求DTO
export interface AiProviderConfigAddDTO {
  configName: string
  providerCode: string
  protocolType: ProtocolType
  apiKey: string
  apiEndpoint?: string
  extraConfig?: string
  isDefault?: number
  timeoutSeconds?: number
  maxRetries?: number
  remark?: string
}

// 供应商配置更新请求DTO
export interface AiProviderConfigUpdateDTO {
  id: string
  configName?: string
  apiKey?: string
  apiEndpoint?: string
  extraConfig?: string
  isDefault?: number
  timeoutSeconds?: number
  maxRetries?: number
  status?: number
  remark?: string
}

// 供应商配置列表响应
export interface ProviderConfigListResponse {
  code: number
  message: string
  data: AiProviderConfigVO[]
}

// 添加供应商配置响应
export interface AddProviderConfigResponse {
  code: number
  message: string
  data: string // 返回配置ID（string类型）
}

// 测试连接响应
export interface TestProviderConnectionResponse {
  code: number
  message: string
  data: string
}

// 协议类型配置提示信息
export interface ProtocolConfigTip {
  protocolType: ProtocolType
  displayName: string
  description: string
  apiKeyPlaceholder: string
  endpointPlaceholder: string
  extraConfigFields?: string[]
}

// 协议类型配置提示映射
export const PROTOCOL_CONFIG_TIPS: Record<ProtocolType, ProtocolConfigTip> = {
  OPENAI: {
    protocolType: 'OPENAI',
    displayName: 'OpenAI',
    description: 'OpenAI官方API，支持GPT-4、GPT-3.5等模型',
    apiKeyPlaceholder: 'sk-xxxxxxxxxxxxx',
    endpointPlaceholder: 'https://api.openai.com',
  },
  ANTHROPIC: {
    protocolType: 'ANTHROPIC',
    displayName: 'Anthropic',
    description: 'Anthropic Claude API，支持Claude系列模型',
    apiKeyPlaceholder: 'sk-ant-xxxxxxxxxxxxx',
    endpointPlaceholder: 'https://api.anthropic.com',
  },
  OLLAMA: {
    protocolType: 'OLLAMA',
    displayName: 'Ollama',
    description: '本地部署的Ollama服务，支持多种开源模型',
    apiKeyPlaceholder: '无需API密钥（可选）',
    endpointPlaceholder: 'http://localhost:11434',
  },
  AZURE_OPENAI: {
    protocolType: 'AZURE_OPENAI',
    displayName: 'Azure OpenAI',
    description: '微软Azure云服务上的OpenAI API',
    apiKeyPlaceholder: 'Azure API密钥',
    endpointPlaceholder: 'https://your-resource.openai.azure.com',
    extraConfigFields: ['deploymentName', 'apiVersion'],
  },
  QWEN: {
    protocolType: 'QWEN',
    displayName: '通义千问',
    description: '阿里云通义千问API',
    apiKeyPlaceholder: '阿里云API密钥',
    endpointPlaceholder: 'https://dashscope.aliyuncs.com/api/v1',
  },
  ERNIE: {
    protocolType: 'ERNIE',
    displayName: '文心一言',
    description: '百度文心一言API',
    apiKeyPlaceholder: '百度API密钥',
    endpointPlaceholder: 'https://aip.baidubce.com',
    extraConfigFields: ['secretKey'],
  },
  DEEPSEEK: {
    protocolType: 'DEEPSEEK',
    displayName: 'DeepSeek',
    description: 'DeepSeek API，支持DeepSeek系列模型',
    apiKeyPlaceholder: 'sk-xxxxxxxxxxxxx',
    endpointPlaceholder: 'https://api.deepseek.com',
  },
  CUSTOM: {
    protocolType: 'CUSTOM',
    displayName: '自定义',
    description: '自定义API服务，需要手动配置所有参数',
    apiKeyPlaceholder: '自定义API密钥',
    endpointPlaceholder: '自定义API端点',
    extraConfigFields: ['自定义配置项'],
  },
}