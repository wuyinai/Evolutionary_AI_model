// 技能包Pinia Store

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserSkill } from '@/types/skill'
import {
  uploadSkill,
  getSkillList,
  getSkillDetail,
  updateSkillStatus,
  deleteSkill
} from '@/utils/skillApi'

export const useSkillStore = defineStore('skill', () => {
  // 技能列表
  const skills = ref<UserSkill[]>([])
  
  // 当前选中的技能
  const currentSkill = ref<UserSkill | null>(null)
  
  // 加载状态
  const loading = ref(false)
  
  // 加载技能列表
  const loadSkills = async () => {
    loading.value = true
    try {
      const response = await getSkillList()
      if (response.code === 200) {
        skills.value = response.data
      }
      return response
    } catch (error) {
      console.error('加载技能列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 上传技能包
  const uploadSkillPackage = async (file: File) => {
    try {
      const response = await uploadSkill(file)
      if (response.code === 200) {
        // 刷新列表
        await loadSkills()
      }
      return response
    } catch (error) {
      console.error('上传技能包失败:', error)
      throw error
    }
  }
  
  // 获取技能详情
  const loadSkillDetail = async (skillId: string) => {
    loading.value = true
    try {
      const response = await getSkillDetail(skillId)
      if (response.code === 200) {
        currentSkill.value = response.data
      }
      return response
    } catch (error) {
      console.error('获取技能详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新技能状态
  const toggleSkillStatus = async (skillId: string, enabled: boolean) => {
    try {
      const response = await updateSkillStatus(skillId, enabled)
      if (response.code === 200) {
        // 更新本地列表中的状态
        const skill = skills.value.find(s => s.id === skillId)
        if (skill) {
          skill.enabled = enabled
        }
      }
      return response
    } catch (error) {
      console.error('更新技能状态失败:', error)
      throw error
    }
  }
  
  // 删除技能
  const removeSkill = async (skillId: string) => {
    try {
      const response = await deleteSkill(skillId)
      if (response.code === 200) {
        // 刷新列表
        await loadSkills()
        // 如果删除的是当前技能，清空
        if (currentSkill.value?.id === skillId) {
          currentSkill.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除技能失败:', error)
      throw error
    }
  }
  
  return {
    skills,
    currentSkill,
    loading,
    loadSkills,
    uploadSkillPackage,
    loadSkillDetail,
    toggleSkillStatus,
    removeSkill
  }
})