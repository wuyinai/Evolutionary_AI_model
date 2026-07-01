// 系统菜单/权限API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 系统菜单/权限接口
 */
export interface SysPermission {
  id: string
  parentId: string
  permissionName: string
  permissionCode?: string
  permissionType: number  // 1-目录, 2-菜单, 3-按钮
  path?: string
  component?: string
  icon?: string
  sort: number
  visible: number  // 0-显示, 1-隐藏
  status: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
}

/**
 * 添加菜单/权限请求参数
 */
export interface PermissionAddData {
  parentId: string
  permissionName: string
  permissionCode?: string
  permissionType: number
  path?: string
  component?: string
  icon?: string
  sort?: number
  visible?: number
  status?: number
  remark?: string
}

/**
 * 修改菜单/权限请求参数
 */
export interface PermissionUpdateData {
  id: string
  parentId?: string
  permissionName?: string
  permissionCode?: string
  permissionType?: number
  path?: string
  component?: string
  icon?: string
  sort?: number
  visible?: number
  status?: number
  remark?: string
}

/**
 * 查询所有菜单/权限列表
 */
export const getPermissionList = (): Promise<ApiResponse<SysPermission[]>> => {
  return request.get('/system/permission/list')
}

/**
 * 获取当前登录用户的菜单树（用于动态渲染侧边栏）
 */
export const getUserMenuTree = (): Promise<ApiResponse<SysPermission[]>> => {
  return request.get('/system/permission/user-menu')
}

/**
 * 获取当前登录用户的权限码列表（用于前端按钮权限控制）
 */
export const getUserPermissionCodes = (): Promise<ApiResponse<string[]>> => {
  return request.get('/system/permission/user-codes')
}

/**
 * 添加菜单/权限
 */
export const addPermission = (data: PermissionAddData): Promise<ApiResponse<void>> => {
  return request.post('/system/permission', data)
}

/**
 * 修改菜单/权限
 */
export const updatePermission = (data: PermissionUpdateData): Promise<ApiResponse<void>> => {
  return request.put('/system/permission', data)
}

/**
 * 删除菜单/权限
 */
export const deletePermission = (id: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/permission/${id}`)
}

/**
 * 获取菜单/权限详情
 */
export const getPermissionById = (id: string): Promise<ApiResponse<SysPermission>> => {
  return request.get(`/system/permission/${id}`)
}

/**
 * 获取角色已分配的权限ID列表
 */
export const getRolePermissionIds = (roleId: string): Promise<ApiResponse<string[]>> => {
  return request.get(`/system/permission/role/${roleId}`)
}

/**
 * 更新角色的权限分配
 */
export const updateRolePermissions = (roleId: string, permissionIds: string[]): Promise<ApiResponse<void>> => {
  return request.put(`/system/permission/role/${roleId}`, permissionIds)
}
