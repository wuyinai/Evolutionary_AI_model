// 知识库API接口

import request from './request'
import type { ApiResponse } from '@/types/api'
import type { KnowledgeBase, KnowledgeDocument, CreateKnowledgeBaseDTO, UpdateKnowledgeBaseDTO } from '@/types/knowledge'

/**
 * 创建知识库
 */
export const createKnowledgeBase = (data: CreateKnowledgeBaseDTO): Promise<ApiResponse<string>> => {
  return request.post('/knowledge/base', data)
}

/**
 * 获取用户的知识库列表
 */
export const getKnowledgeBaseList = (): Promise<ApiResponse<KnowledgeBase[]>> => {
  return request.get('/knowledge/base/list')
}

/**
 * 获取知识库详情
 */
export const getKnowledgeBaseDetail = (id: string): Promise<ApiResponse<KnowledgeBase>> => {
  return request.get(`/knowledge/base/${id}`)
}

/**
 * 更新知识库
 */
export const updateKnowledgeBase = (data: UpdateKnowledgeBaseDTO): Promise<ApiResponse<void>> => {
  return request.put('/knowledge/base', data)
}

/**
 * 删除知识库
 */
export const deleteKnowledgeBase = (id: string): Promise<ApiResponse<void>> => {
  return request.delete(`/knowledge/base/${id}`)
}

/**
 * 获取知识库下的文档列表
 */
export const getKnowledgeBaseDocuments = (knowledgeBaseId: string): Promise<ApiResponse<KnowledgeDocument[]>> => {
  return request.get(`/knowledge/base/${knowledgeBaseId}/documents`)
}

/**
 * 上传文档到知识库
 */
export const uploadDocumentToKnowledgeBase = (
  file: File,
  knowledgeBaseId: string,
  embeddingModelId?: string,
  securityLabelId?: string
): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('knowledgeBaseId', knowledgeBaseId)
  formData.append('securityLabelId', securityLabelId || '')
  if (embeddingModelId) {
    formData.append('embeddingModelId', embeddingModelId)
  }
  
  return request.post('/knowledge/document/upload-to-base', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文档列表（所有文档）
 */
export const getDocumentList = (): Promise<ApiResponse<KnowledgeDocument[]>> => {
  return request.get('/knowledge/document/list')
}

/**
 * 获取独立文档列表（不属于任何知识库）
 */
export const getStandaloneDocuments = (): Promise<ApiResponse<KnowledgeDocument[]>> => {
  return request.get('/knowledge/document/standalone')
}

/**
 * 获取文档状态
 */
export const getDocumentStatus = (documentId: string): Promise<ApiResponse<KnowledgeDocument>> => {
  return request.get(`/knowledge/document/status/${documentId}`)
}

/**
 * 删除文档
 */
export const deleteDocument = (documentId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/knowledge/document/${documentId}`)
}

/**
 * 重新处理文档
 */
export const reprocessDocument = (documentId: string): Promise<ApiResponse<void>> => {
  return request.post(`/knowledge/document/reprocess/${documentId}`)
}