// System Department API

import request from './request'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { KnowledgeBase } from '@/types/knowledge'

/**
 * System Department Interface
 */
export interface SysDept {
  id: string
  parentId: string
  ancestors?: string
  deptName: string
  deptCode?: string
  sort: number
  leader?: string
  leaderId?: string
  phone?: string
  email?: string
  status: number
  createTime?: string
  remark?: string
}

/**
 * Department Tree Node (with children and expanded state)
 */
export interface DeptTreeNode extends SysDept {
  children?: DeptTreeNode[]
  expanded?: boolean
}

/**
 * Department Add Data
 */
export interface DeptAddData {
  parentId: string
  deptName: string
  deptCode?: string
  sort?: number
  leader?: string
  leaderId?: string
  phone?: string
  email?: string
  status?: number
  remark?: string
  knowledgeBaseIds?: string[]
}

/**
 * Department Update Data
 */
export interface DeptUpdateData {
  id: string
  parentId?: string
  deptName?: string
  deptCode?: string
  sort?: number
  leader?: string
  leaderId?: string
  phone?: string
  email?: string
  status?: number
  remark?: string
  knowledgeBaseIds?: string[]
}

/**
 * Get all department list
 */
export const getDeptList = (): Promise<ApiResponse<SysDept[]>> => {
  return request.get('/system/dept/list')
}

/**
 * Get department tree structure (supports filtering)
 */
export const getDeptTree = (
  deptName?: string,
  deptCode?: string,
  status?: number
): Promise<ApiResponse<DeptTreeNode[]>> => {
  const params: Record<string, unknown> = {}
  if (deptName) params.deptName = deptName
  if (deptCode) params.deptCode = deptCode
  if (status !== undefined && status !== null) params.status = status
  return request.get('/system/dept/tree', { params })
}

/**
 * Get department page list with filters
 */
export const getDeptPage = (
  pageNum: number,
  pageSize: number,
  deptName?: string,
  deptCode?: string,
  status?: number,
  parentId?: string
): Promise<ApiResponse<PageResponse<SysDept>>> => {
  const params: Record<string, unknown> = {
    pageNum,
    pageSize
  }
  if (deptName) params.deptName = deptName
  if (deptCode) params.deptCode = deptCode
  if (status !== undefined) params.status = status
  if (parentId) params.parentId = parentId
  return request.get('/system/dept/page', { params })
}

/**
 * Get department detail by ID
 */
export const getDeptById = (deptId: string): Promise<ApiResponse<SysDept>> => {
  return request.get(`/system/dept/${deptId}`)
}

/**
 * Add department
 */
export const addDept = (data: DeptAddData): Promise<ApiResponse<void>> => {
  return request.post('/system/dept', data)
}

/**
 * Update department
 */
export const updateDept = (data: DeptUpdateData): Promise<ApiResponse<void>> => {
  return request.put('/system/dept', data)
}

/**
 * Delete department
 */
export const deleteDept = (deptId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/dept/${deptId}`)
}

/**
 * Get user IDs under department
 */
export const getDeptUserIds = (deptId: string): Promise<ApiResponse<string[]>> => {
  return request.get(`/system/dept/${deptId}/users`)
}

/**
 * Batch assign users to department
 */
export const assignUsersToDept = (deptId: string, userIds: string[]): Promise<ApiResponse<void>> => {
  return request.post(`/system/dept/${deptId}/users`, userIds)
}

/**
 * Batch assign users by roles to department
 */
export const assignUsersByRolesToDept = (deptId: string, roleIds: string[]): Promise<ApiResponse<void>> => {
  return request.post(`/system/dept/${deptId}/users/byRoles`, roleIds)
}

/**
 * Remove user-department association
 */
export const removeUsersFromDept = (deptId: string, userIds: string[]): Promise<ApiResponse<void>> => {
  return request.delete(`/system/dept/${deptId}/users`, { data: userIds })
}

/**
 * Get all knowledge bases (for department modal)
 */
export const getAllKnowledgeBases = (): Promise<ApiResponse<KnowledgeBase[]>> => {
  return request.get('/knowledge/base/all')
}

/**
 * Get knowledge base IDs associated with a department
 */
export const getDeptKnowledgeBaseIds = (deptId: string): Promise<ApiResponse<string[]>> => {
  return request.get(`/system/dept/${deptId}/knowledge-bases`)
}