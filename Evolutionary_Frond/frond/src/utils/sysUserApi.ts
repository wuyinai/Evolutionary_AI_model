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
 * 用户添加请求参数
 */
export interface UserAddData {
  username: string
  password: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  gender?: number
  status?: number
  deptId?: string
  remark?: string
}

/**
 * 用户修改请求参数
 */
export interface UserUpdateData {
  id: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  gender?: number
  status?: number
  deptId?: string
  remark?: string
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

/**
 * 根据ID查询用户信息
 */
export const getUserById = (userId: string): Promise<ApiResponse<SysUser>> => {
  return request.get(`/system/user/${userId}`)
}

/**
 * 添加用户
 */
export const addUser = (data: UserAddData): Promise<ApiResponse<void>> => {
  return request.post('/system/user', data)
}

/**
 * 修改用户信息
 */
export const updateUser = (data: UserUpdateData): Promise<ApiResponse<void>> => {
  return request.put('/system/user', data)
}

/**
 * 删除用户
 */
export const deleteUser = (userId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/user/${userId}`)
}

/**
 * 上传头像
 */
export const uploadAvatar = (file: File): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}