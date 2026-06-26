// AI角色相关类型定义

/**
 * AI角色接口
 */
export interface AiRole {
  id: string
  roleName: string
  roleCode: string
  description?: string
  systemPrompt?: string
  systemPromptTemplate?: string
  userId: string
  status: number | string // 后端返回0/1，前端显示为ACTIVE/INACTIVE
  createTime: string
  updateTime: string
  delFlag?: number
  documents?: AiRoleDocument[]
}

/**
 * 角色文档接口
 */
export interface AiRoleDocument {
  id: string
  roleId: string
  documentName: string
  documentPath: string
  documentType?: string
  documentSize?: number
  documentContent?: string
  uploadTime?: string
  delFlag?: number
}

/**
 * 创建AI角色DTO
 */
export interface CreateAiRoleDTO {
  roleName: string
  roleCode: string
  description?: string
  systemPrompt?: string
  systemPromptTemplate?: string
}

/**
 * 更新AI角色DTO
 */
export interface UpdateAiRoleDTO {
  id: string
  roleName?: string
  description?: string
  systemPrompt?: string
  systemPromptTemplate?: string
  status?: string
}

/**
 * 角色文档预览响应
 */
export interface DocumentPreviewResponse {
  documentId: string
  documentName: string
  content: string
}
