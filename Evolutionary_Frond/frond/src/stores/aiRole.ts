// AI角色Pinia Store

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  AiRole,
  AiRoleDocument,
  CreateAiRoleDTO,
  UpdateAiRoleDTO,
  DocumentPreviewResponse
} from '@/types/aiRole'
import {
  createAiRole,
  getAiRoleList,
  getAiRoleDetail,
  updateAiRole,
  deleteAiRole,
  uploadDocumentToRole,
  deleteRoleDocument,
  getRoleDocuments,
  getDocumentDetail,
  buildSystemPrompt
} from '@/utils/aiRoleApi'

export const useAiRoleStore = defineStore('aiRole', () => {
  // AI角色列表
  const roles = ref<AiRole[]>([])

  // 当前选中的角色
  const currentRole = ref<AiRole | null>(null)

  // 当前角色的文档列表
  const currentDocuments = ref<AiRoleDocument[]>([])

  // 加载状态
  const loading = ref(false)

  // 上传状态
  const uploading = ref(false)

  // 预览的文档内容
  const previewContent = ref<DocumentPreviewResponse | null>(null)

  // 构建后的系统提示词
  const builtSystemPrompt = ref<string>('')

  // 加载角色列表
  const loadRoles = async () => {
    loading.value = true
    try {
      const response = await getAiRoleList()
      if (response.code === 200) {
        roles.value = response.data
      }
      return response
    } catch (error) {
      console.error('加载角色列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建角色
  const createRole = async (data: CreateAiRoleDTO) => {
    try {
      const response = await createAiRole(data)
      if (response.code === 200) {
        // 刷新列表
        await loadRoles()
      }
      return response
    } catch (error) {
      console.error('创建角色失败:', error)
      throw error
    }
  }

  // 获取角色详情
  const loadRoleDetail = async (id: string) => {
    loading.value = true
    try {
      const response = await getAiRoleDetail(id)
      if (response.code === 200) {
        currentRole.value = response.data
        // 如果响应中包含文档列表，也加载文档
        if (response.data.documents) {
          currentDocuments.value = response.data.documents
        }
      }
      return response
    } catch (error) {
      console.error('获取角色详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新角色
  const updateRole = async (data: UpdateAiRoleDTO) => {
    try {
      const response = await updateAiRole(data)
      if (response.code === 200) {
        // 刷新列表
        await loadRoles()
        // 如果更新的是当前角色，也刷新详情
        if (currentRole.value && currentRole.value.id === data.id) {
          await loadRoleDetail(data.id)
        }
      }
      return response
    } catch (error) {
      console.error('更新角色失败:', error)
      throw error
    }
  }

  // 删除角色
  const deleteRole = async (id: string) => {
    try {
      const response = await deleteAiRole(id)
      if (response.code === 200) {
        // 刷新列表
        await loadRoles()
        // 如果删除的是当前角色，清空当前角色
        if (currentRole.value && currentRole.value.id === id) {
          currentRole.value = null
          currentDocuments.value = []
        }
      }
      return response
    } catch (error) {
      console.error('删除角色失败:', error)
      throw error
    }
  }

  // 上传文档到角色
  const uploadDocument = async (file: File, roleId: string) => {
    uploading.value = true
    try {
      const response = await uploadDocumentToRole(file, roleId)
      if (response.code === 200) {
        // 刷新文档列表
        await loadRoleDocuments(roleId)
      }
      return response
    } catch (error) {
      console.error('上传文档失败:', error)
      throw error
    } finally {
      uploading.value = false
    }
  }

  // 加载角色的文档列表
  const loadRoleDocuments = async (roleId: string) => {
    try {
      const response = await getRoleDocuments(roleId)
      if (response.code === 200) {
        currentDocuments.value = response.data
      }
      return response
    } catch (error) {
      console.error('加载文档列表失败:', error)
      throw error
    }
  }

  // 删除角色文档
  const deleteDocument = async (documentId: string) => {
    try {
      const response = await deleteRoleDocument(documentId)
      if (response.code === 200) {
        // 从当前文档列表中移除
        currentDocuments.value = currentDocuments.value.filter(doc => doc.id !== documentId)
      }
      return response
    } catch (error) {
      console.error('删除文档失败:', error)
      throw error
    }
  }

  // 预览文档内容（获取文档实体，显示documentContent）
  const previewDoc = async (documentId: string) => {
    loading.value = true
    try {
      const response = await getDocumentDetail(documentId)
      if (response.code === 200 && response.data) {
        // 使用documentContent字段作为预览内容
        previewContent.value = {
          documentId: response.data.id,
          documentName: response.data.documentName,
          content: response.data.documentContent || '文档内容未解析或为空'
        }
      }
      return response
    } catch (error) {
      console.error('预览文档失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 构建系统提示词
  const buildPrompt = async (roleId: string) => {
    try {
      const response = await buildSystemPrompt(roleId)
      if (response.code === 200) {
        builtSystemPrompt.value = response.data
      }
      return response
    } catch (error) {
      console.error('构建系统提示词失败:', error)
      throw error
    }
  }

  // 清空预览内容
  const clearPreview = () => {
    previewContent.value = null
  }

  // 清空构建的提示词
  const clearBuiltPrompt = () => {
    builtSystemPrompt.value = ''
  }

  return {
    roles,
    currentRole,
    currentDocuments,
    loading,
    uploading,
    previewContent,
    builtSystemPrompt,
    loadRoles,
    createRole,
    loadRoleDetail,
    updateRole,
    deleteRole,
    uploadDocument,
    loadRoleDocuments,
    deleteDocument,
    previewDoc,
    buildPrompt,
    clearPreview,
    clearBuiltPrompt
  }
})