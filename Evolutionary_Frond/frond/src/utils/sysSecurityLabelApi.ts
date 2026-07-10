// 安全密级标签API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 密级标签接口
 */
export interface SecurityLabel {
  id: string
  labelName: string
  labelCode: string
  labelLevel: number
  description?: string
  createBy?: string
  createTime?: string
}

/**
 * 查询所有密级标签列表
 */
export const getSecurityLabelList = (): Promise<ApiResponse<SecurityLabel[]>> => {
  return request.get('/system/security-label/list')
}
