// 系统提示词Pinia Store

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { SysPrompt, CreateTextPromptDTO, UpdateSysPromptDTO } from '@/types/sysPrompt'
import {
  uploadDocumentPrompt,
  createTextPrompt,
  getSysPromptList,
  getSysPromptDetail,
  updateSysPrompt,
  deleteSysPrompt,
  getDocumentPreviewUrl,
  updatePromptEnabled,
  setDefaultPrompt
} from '@/utils/sysPromptApi'

export const useSysPromptStore = defineStore('sysPrompt', () => {
  const prompts = ref<SysPrompt[]>([])
  const currentPrompt = ref<SysPrompt | null>(null)
  const loading = ref(false)
  const uploading = ref(false)
  const previewUrl = ref<string>('')

  const loadPrompts = async () => {
    loading.value = true
    try {
      const response = await getSysPromptList()
      if (response.code === 200) {
        prompts.value = response.data
      }
      return response
    } catch (error) {
      console.error('加载提示词列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const uploadDocument = async (
    promptName: string,
    promptCode: string,
    promptDescription: string,
    file: File
  ) => {
    uploading.value = true
    try {
      const response = await uploadDocumentPrompt(promptName, promptCode, promptDescription, file)
      if (response.code === 200) {
        await loadPrompts()
      }
      return response
    } catch (error) {
      console.error('上传文档型提示词失败:', error)
      throw error
    } finally {
      uploading.value = false
    }
  }

  const createText = async (data: CreateTextPromptDTO) => {
    try {
      const response = await createTextPrompt(data)
      if (response.code === 200) {
        await loadPrompts()
      }
      return response
    } catch (error) {
      console.error('创建文本型提示词失败:', error)
      throw error
    }
  }

  const loadPromptDetail = async (id: string) => {
    loading.value = true
    try {
      const response = await getSysPromptDetail(id)
      if (response.code === 200) {
        currentPrompt.value = response.data
      }
      return response
    } catch (error) {
      console.error('获取提示词详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updatePrompt = async (id: string, data: UpdateSysPromptDTO) => {
    try {
      const response = await updateSysPrompt(id, data)
      if (response.code === 200) {
        await loadPrompts()
        if (currentPrompt.value && currentPrompt.value.id === id) {
          await loadPromptDetail(id)
        }
      }
      return response
    } catch (error) {
      console.error('更新提示词失败:', error)
      throw error
    }
  }

  const deletePrompt = async (id: string) => {
    try {
      const response = await deleteSysPrompt(id)
      if (response.code === 200) {
        await loadPrompts()
        if (currentPrompt.value && currentPrompt.value.id === id) {
          currentPrompt.value = null
        }
      }
      return response
    } catch (error) {
      console.error('删除提示词失败:', error)
      throw error
    }
  }

  const loadPreviewUrl = async (id: string) => {
    loading.value = true
    try {
      const response = await getDocumentPreviewUrl(id)
      if (response.code === 200) {
        previewUrl.value = response.data
      }
      return response
    } catch (error) {
      console.error('获取文档预览URL失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const toggleEnabled = async (id: string, enabled: number) => {
    try {
      const response = await updatePromptEnabled(id, enabled)
      if (response.code === 200) {
        await loadPrompts()
      }
      return response
    } catch (error) {
      console.error('更新提示词启用状态失败:', error)
      throw error
    }
  }

  const setAsDefault = async (id: string) => {
    try {
      const response = await setDefaultPrompt(id)
      if (response.code === 200) {
        await loadPrompts()
      }
      return response
    } catch (error) {
      console.error('设置默认提示词失败:', error)
      throw error
    }
  }

  const clearPreviewUrl = () => {
    previewUrl.value = ''
  }

  const clearCurrentPrompt = () => {
    currentPrompt.value = null
  }

  return {
    prompts,
    currentPrompt,
    loading,
    uploading,
    previewUrl,
    loadPrompts,
    uploadDocument,
    createText,
    loadPromptDetail,
    updatePrompt,
    deletePrompt,
    loadPreviewUrl,
    toggleEnabled,
    setAsDefault,
    clearPreviewUrl,
    clearCurrentPrompt
  }
})