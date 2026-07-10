// 系统审批API接口

import request from './request'
import type { ApiResponse } from '@/types/api'

/**
 * 系统审批接口
 */
export interface SysApproval {
  id: string
  approvalType: string
  approvalTitle: string
  approvalContent?: string
  applicantId: string
  applicantName?: string
  approverId?: string
  approverName?: string
  approvalStatus: number
  approvalTime?: string
  approvalOpinion?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
}

/**
 * 系统字典接口
 */
export interface SysDict {
  id: string
  dictType: string
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
 * 分页查询审批列表
 */
export const getApprovalList = (
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<SysApproval>>> => {
  return request.get(`/system/approval/list?page=${page}&size=${size}`)
}

/**
 * 根据审批类型分页查询审批列表
 */
export const getApprovalListByType = (
  approvalType: string,
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<SysApproval>>> => {
  return request.get(`/system/approval/list/type/${approvalType}?page=${page}&size=${size}`)
}

/**
 * 根据审批状态分页查询审批列表
 */
export const getApprovalListByStatus = (
  approvalStatus: number,
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<SysApproval>>> => {
  return request.get(`/system/approval/list/status/${approvalStatus}?page=${page}&size=${size}`)
}

/**
 * 根据申请人ID分页查询审批列表
 */
export const getApprovalListByApplicant = (
  applicantId: string,
  page: number = 1,
  size: number = 10
): Promise<ApiResponse<PageResponse<SysApproval>>> => {
  return request.get(`/system/approval/list/applicant/${applicantId}?page=${page}&size=${size}`)
}

/**
 * 获取审批详情
 */
export const getApprovalById = (approvalId: string): Promise<ApiResponse<SysApproval>> => {
  return request.get(`/system/approval/${approvalId}`)
}

/**
 * 查询审批类型字典列表
 */
export const getApprovalTypes = (): Promise<ApiResponse<SysDict[]>> => {
  return request.get('/system/approval/dict/types')
}

/**
 * 查询审批状态字典列表
 */
export const getApprovalStatuses = (): Promise<ApiResponse<SysDict[]>> => {
  return request.get('/system/approval/dict/statuses')
}