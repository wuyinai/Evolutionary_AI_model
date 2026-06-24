// 技能包相关类型定义

/**
 * 用户技能接口
 * ID使用string类型，避免JavaScript精度丢失（后端Long类型序列化为字符串）
 */
export interface UserSkill {
  id: string
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
  skillId: string
  enabled: boolean
}