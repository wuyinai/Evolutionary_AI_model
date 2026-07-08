// 系统提示词API接口

import request from './request'
import type { ApiResponse } from '@/types/api'
import type { SysPrompt, CreateTextPromptDTO, UpdateSysPromptDTO } from '@/types/sysPrompt'

export const uploadDocumentPrompt = (
  promptName: string,
  promptCode: string,
  promptDescription: string,
  file: File
): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('promptName', promptName)
  formData.append('promptCode', promptCode)
  formData.append('promptDescription', promptDescription)
  formData.append('file', file)
  return request.post('/system/prompt/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const createTextPrompt = (data: CreateTextPromptDTO): Promise<ApiResponse<string>> => {
  return request.post('/system/prompt/text', data)
}

export const getSysPromptList = (): Promise<ApiResponse<SysPrompt[]>> => {
  return request.get('/system/prompt/list')
}

export const getSysPromptDetail = (id: string): Promise<ApiResponse<SysPrompt>> => {
  return request.get(`/system/prompt/${id}`)
}

export const updateSysPrompt = (id: string, data: UpdateSysPromptDTO): Promise<ApiResponse<void>> => {
  return request.put(`/system/prompt/${id}`, data)
}

export const deleteSysPrompt = (id: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/prompt/${id}`)
}

export const getDocumentPreviewUrl = (id: string, expiry: number = 3600): Promise<ApiResponse<string>> => {
  return request.get(`/system/prompt/preview/${id}?expiry=${expiry}`)
}

export const updatePromptEnabled = (id: string, enabled: number): Promise<ApiResponse<void>> => {
  return request.put(`/system/prompt/${id}/enabled?enabled=${enabled}`)
}

export const setDefaultPrompt = (id: string): Promise<ApiResponse<void>> => {
  return request.put(`/system/prompt/${id}/default`)
}