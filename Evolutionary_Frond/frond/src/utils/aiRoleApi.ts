// AI角色API接口

import request from './request'
import type { ApiResponse } from '@/types/api'
import type {
  AiRole,
  AiRoleDocument,
  CreateAiRoleDTO,
  UpdateAiRoleDTO,
  DocumentPreviewResponse
} from '@/types/aiRole'

/**
 * 创建AI角色
 */
export const createAiRole = (data: CreateAiRoleDTO): Promise<ApiResponse<string>> => {
  return request.post('/ai-role', data)
}

/**
 * 获取用户的AI角色列表
 */
export const getAiRoleList = (): Promise<ApiResponse<AiRole[]>> => {
  return request.get('/ai-role/list')
}

/**
 * 获取AI角色详情（包含文档列表）
 */
export const getAiRoleDetail = (id: string): Promise<ApiResponse<AiRole>> => {
  return request.get(`/ai-role/${id}`)
}

/**
 * 更新AI角色
 */
export const updateAiRole = (data: UpdateAiRoleDTO): Promise<ApiResponse<void>> => {
  return request.put('/ai-role', data)
}

/**
 * 删除AI角色
 */
export const deleteAiRole = (id: string): Promise<ApiResponse<void>> => {
  return request.delete(`/ai-role/${id}`)
}

/**
 * 上传文档到角色
 */
export const uploadDocumentToRole = (
  file: File,
  roleId: string
): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('file', file)

  // roleId通过URL路径传递，不在FormData中
  return request.post(`/ai-role/${roleId}/document`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除角色文档
 */
export const deleteRoleDocument = (documentId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/ai-role/document/${documentId}`)
}

/**
 * 获取角色的文档列表
 */
export const getRoleDocuments = (roleId: string): Promise<ApiResponse<AiRoleDocument[]>> => {
  return request.get(`/ai-role/${roleId}/documents`)
}

/**
 * 获取文档详情（包含解析后的内容）
 */
export const getDocumentDetail = (documentId: string): Promise<ApiResponse<AiRoleDocument>> => {
  return request.get(`/ai-role/document/${documentId}`)
}

/**
 * 预览文档内容（获取预览URL）
 */
export const previewDocument = (documentId: string): Promise<ApiResponse<string>> => {
  return request.get(`/ai-role/document/${documentId}/preview`)
}

/**
 * 构建系统提示词（预览）
 */
export const buildSystemPrompt = (roleId: string): Promise<ApiResponse<string>> => {
  return request.get(`/ai-role/${roleId}/preview-prompt`)
}

/**
 * 根据角色代码获取角色
 */
export const getRoleByCode = (roleCode: string): Promise<ApiResponse<AiRole>> => {
  return request.get(`/ai-role/code/${roleCode}`)
}