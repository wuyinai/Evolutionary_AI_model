// 知识库相关类型定义

/**
 * 知识库接口
 */
export interface KnowledgeBase {
  id: string
  name: string
  description?: string
  userId: string
  embeddingModelId?: string
  documentCount: number
  chunkCount: number
  status: string // ACTIVE-活跃 INACTIVE-停用
  createTime: string
  updateTime: string
  delFlag?: number
}

/**
 * 知识库文档接口
 */
export interface KnowledgeDocument {
  id: string
  documentName: string
  userId: string
  knowledgeBaseId?: string
  fileType: string
  fileSize: number
  storagePath: string
  embeddingModelId?: string
  status: string // PENDING-待处理 PROCESSING-处理中 COMPLETED-已完成 FAILED-失败
  chunkCount: number
  errorMessage?: string
  createTime: string
  updateTime: string
  delFlag?: number
}

/**
 * 创建知识库DTO
 */
export interface CreateKnowledgeBaseDTO {
  name: string
  description?: string
  embeddingModelId?: string
  securityLabelId?: string
}

/**
 * 更新知识库DTO
 */
export interface UpdateKnowledgeBaseDTO {
  id: string
  name?: string
  description?: string
  embeddingModelId?: string
  status?: string
}

/**
 * 上传文档到知识库请求
 */
export interface UploadDocumentToBaseRequest {
  knowledgeBaseId: string
  embeddingModelId?: string
}