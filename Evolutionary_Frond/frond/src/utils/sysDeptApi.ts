// 系统部门API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 系统部门接口
 */
export interface SysDept {
  id: string
  parentId: string
  ancestors?: string
  deptName: string
  deptCode?: string
  sort: number
  leader?: string
  phone?: string
  email?: string
  status: number
  createTime?: string
  remark?: string
}

/**
 * 查询所有部门列表
 */
export const getDeptList = (): Promise<ApiResponse<SysDept[]>> => {
  return request.get('/system/dept/list')
}