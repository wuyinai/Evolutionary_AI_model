// 系统角色API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 系统角色接口
 */
export interface SysRole {
  id: string
  roleName: string
  roleCode: string
  roleSort: number
  dataScope: number
  permControl: number
  status: number
  securityLabelId?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
}

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
  lastLoginTime?: string
  lastLoginIp?: string
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
 * 分页查询角色列表
 */
export const getRoleList = (
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<SysRole>>> => {
  return request.get(`/system/role/list?page=${page}&size=${size}`)
}

/**
 * 添加角色
 */
export const addRole = (data: {
  roleName: string
  roleCode: string
  roleSort?: number
  dataScope?: number
  permControl?: number
  status?: number
  remark?: string
  securityLabelId?: string
}): Promise<ApiResponse<void>> => {
  return request.post('/system/role', data)
}

/**
 * 修改角色
 */
export const updateRole = (data: {
  id: string
  roleName?: string
  roleCode?: string
  roleSort?: number
  dataScope?: number
  permControl?: number
  status?: number
  remark?: string
  securityLabelId?: string
}): Promise<ApiResponse<void>> => {
  return request.put('/system/role', data)
}

/**
 * 删除角色
 */
export const deleteRole = (roleId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/role/${roleId}`)
}

/**
 * 获取角色详情
 */
export const getRoleById = (roleId: string): Promise<ApiResponse<SysRole>> => {
  return request.get(`/system/role/${roleId}`)
}

/**
 * 查询角色下的用户列表
 */
export const getUsersByRoleId = (roleId: string): Promise<ApiResponse<SysUser[]>> => {
  return request.get(`/system/role/${roleId}/users`)
}

/**
 * 为角色分配用户（批量添加用户到角色）
 */
export const assignUsersToRole = (
  roleId: string,
  userIds: string[]
): Promise<ApiResponse<void>> => {
  return request.post(`/system/role/${roleId}/users`, userIds)
}

/**
 * 从角色中移除用户
 */
export const removeUserFromRole = (
  roleId: string,
  userId: string
): Promise<ApiResponse<void>> => {
  return request.delete(`/system/role/${roleId}/users/${userId}`)
}

/**
 * 批量从角色中移除用户
 */
export const removeUsersFromRole = (
  roleId: string,
  userIds: string[]
): Promise<ApiResponse<void>> => {
  return request.delete(`/system/role/${roleId}/users`, { data: userIds })
}