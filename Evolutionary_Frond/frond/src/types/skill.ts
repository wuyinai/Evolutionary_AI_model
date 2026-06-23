// 技能包相关类型定义

/**
 * 用户技能接口
 */
export interface UserSkill {
  id: number
  name: string
  displayName?: string
  description: string
  version?: string
  author?: string
  enabled: boolean
  createTime: string
  updateTime: string
}

/**
 * 技能状态更新DTO
 */
export interface UpdateSkillStatusDTO {
  skillId: number
  enabled: boolean
}