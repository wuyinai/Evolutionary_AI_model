// 技能包API接口

import request from './request'
import type { ApiResponse } from '@/types/api'
import type { UserSkill } from '@/types/skill'

/**
 * 上传技能包
 */
export const uploadSkill = (file: File): Promise<ApiResponse<number>> => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/skills/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取用户的技能列表
 */
export const getSkillList = (): Promise<ApiResponse<UserSkill[]>> => {
  return request.get('/skills/list')
}

/**
 * 获取技能详情
 */
export const getSkillDetail = (skillId: number): Promise<ApiResponse<UserSkill>> => {
  return request.get(`/skills/${skillId}`)
}

/**
 * 更新技能状态（启用/禁用）
 */
export const updateSkillStatus = (skillId: number, enabled: boolean): Promise<ApiResponse<void>> => {
  return request.put(`/skills/${skillId}/status`, null, {
    params: { enabled }
  })
}

/**
 * 删除技能
 */
export const deleteSkill = (skillId: number): Promise<ApiResponse<void>> => {
  return request.delete(`/skills/${skillId}`)
}