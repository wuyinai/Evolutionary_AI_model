// 操作日志API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 操作日志接口
 */
export interface OperationLog {
  id: string
  userId: string
  username: string
  operation: string
  method: string
  requestMethod: string
  requestUrl: string
  requestParams?: string
  requestTime: number
  ip: string
  location?: string
  browser: string
  os: string
  status: number
  errorMsg?: string
  createTime: string
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
 * 分页查询操作日志
 * @param page 页码
 * @param size 每页大小
 */
export const getOperationLogList = (
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<OperationLog>>> => {
  return request.get(`/system/log/list?page=${page}&size=${size}`)
}

/**
 * 删除单条操作日志
 * @param id 日志ID
 */
export const deleteOperationLog = (id: string): Promise<ApiResponse<void>> => {
  return request.delete(`/system/log/${id}`)
}

/**
 * 批量删除操作日志
 * @param ids 日志ID列表
 */
export const batchDeleteOperationLogs = (ids: string[]): Promise<ApiResponse<void>> => {
  return request.delete('/system/log/batch', { data: ids })
}

/**
 * 清空所有操作日志
 */
export const clearOperationLogs = (): Promise<ApiResponse<void>> => {
  return request.delete('/system/log/clear')
}

/**
 * 获取操作日志详情
 * @param id 日志ID
 */
export const getOperationLogDetail = (id: string): Promise<ApiResponse<OperationLog>> => {
  return request.get(`/system/log/${id}`)
}