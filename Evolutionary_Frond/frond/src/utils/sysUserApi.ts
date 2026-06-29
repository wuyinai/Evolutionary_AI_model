// 系统用户API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 系统用户接口
 */
export interface SysUser {
  id: string
  username: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  gender?: number
  status: number
  deptId?: string
  createTime?: string
  remark?: string
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页查询用户列表（支持部门筛选）
 */
export const getUserList = (
  page: number = 1,
  size: number = 10,
  deptId?: string
): Promise<ApiResponse<PageResponse<SysUser>>> => {
  const params = new URLSearchParams()
  params.append('page', page.toString())
  params.append('size', size.toString())
  if (deptId) params.append('deptId', deptId)
  return request.get(`/system/user/list?${params.toString()}`)
}