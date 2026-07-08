// 系统提示词相关类型定义

/**
 * 系统提示词接口
 */
export interface SysPrompt {
  id: string
  promptName: string
  promptCode?: string
  promptDescription?: string
  promptType: 'DOCUMENT' | 'TEXT'
  documentName?: string
  documentPath?: string
  documentType?: string
  documentSize?: number
  documentContent?: string
  textContent?: string
  isEnabled: number
  isDefault: number
  sortOrder?: number
  uploadTime?: string
  createBy?: string
  createTime: string
  updateBy?: string
  updateTime: string
  delFlag?: number
  remark?: string
}

/**
 * 创建文本型提示词DTO
 */
export interface CreateTextPromptDTO {
  promptName: string
  promptCode?: string
  promptDescription?: string
  textContent: string
}

/**
 * 更新提示词DTO
 */
export interface UpdateSysPromptDTO {
  id: string
  promptName?: string
  promptCode?: string
  promptDescription?: string
  textContent?: string
}