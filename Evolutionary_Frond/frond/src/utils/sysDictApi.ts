// 系统字典API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 系统字典接口
 */
export interface SysDict {
  id: string
  dictType: string
  dictName?: string
  dictCode: string
  dictLabel: string
  dictValue?: string
  sort: number
  status: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
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
 * 分页查询字典类型列表
 */
export const getDictTypesList = (
  dictType?: string,
  dictName?: string,
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<SysDict>>> => {
  const params = new URLSearchParams()
  if (dictType) params.append('dictType', dictType)
  if (dictName) params.append('dictName', dictName)
  params.append('page', page.toString())
  params.append('size', size.toString())
  return request.get(`/system/dict/types/list?${params.toString()}`)
}

/**
 * 根据字典类型查询字典项列表
 */
export const getDictItemsByType = (dictType: string): Promise<ApiResponse<SysDict[]>> => {
  return request.get(`/system/dict/items/${dictType}`)
}

/**
 * 根据ID查询字典详情
 */
export const getDictById = (dictId: string): Promise<ApiResponse<SysDict>> => {
  return request.get(`/system/dict/${dictId}`)
}

/**
 * 新增字典类型
 */
export const addDictType = (sysDict: SysDict): Promise<ApiResponse<void>> => {
  return request.post('/system/dict/type', sysDict)
}

/**
 * 修改字典类型
 */
export const updateDictType = (sysDict: SysDict): Promise<ApiResponse<void>> => {
  return request.put('/system/dict/type', sysDict)
}

/**
 * 删除字典类型
 */
export const deleteDictType = (dictType: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/dict/type/${dictType}`)
}

/**
 * 新增字典项
 */
export const addDictItem = (sysDict: SysDict): Promise<ApiResponse<void>> => {
  return request.post('/system/dict/item', sysDict)
}

/**
 * 修改字典项
 */
export const updateDictItem = (sysDict: SysDict): Promise<ApiResponse<void>> => {
  return request.put('/system/dict/item', sysDict)
}

/**
 * 删除字典项
 */
export const deleteDictItem = (dictId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/dict/item/${dictId}`)
}