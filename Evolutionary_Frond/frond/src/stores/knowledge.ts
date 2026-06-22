// 知识库Pinia Store

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { KnowledgeBase, KnowledgeDocument, CreateKnowledgeBaseDTO, UpdateKnowledgeBaseDTO } from '@/types/knowledge'
import {
  createKnowledgeBase,
  getKnowledgeBaseList,
  getKnowledgeBaseDetail,
  updateKnowledgeBase,
  deleteKnowledgeBase,
  getKnowledgeBaseDocuments,
  uploadDocumentToKnowledgeBase,
  getDocumentList,
  deleteDocument,
  reprocessDocument
} from '@/utils/knowledgeApi'

export const useKnowledgeStore = defineStore('knowledge', () => {
  // 知识库列表
  const knowledgeBases = ref<KnowledgeBase[]>([])
  
  // 当前选中的知识库
  const currentKnowledgeBase = ref<KnowledgeBase | null>(null)
  
  // 当前知识库的文档列表
  const currentDocuments = ref<KnowledgeDocument[]>([])
  
  // 加载状态
  const loading = ref(false)
  
  // 加载知识库列表
  const loadKnowledgeBases = async () => {
    loading.value = true
    try {
      const response = await getKnowledgeBaseList()
      if (response.code === 200) {
        knowledgeBases.value = response.data
      }
      return response
    } catch (error) {
      console.error('加载知识库列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 创建知识库
  const createKB = async (data: CreateKnowledgeBaseDTO) => {
    try {
      const response = await createKnowledgeBase(data)
      if (response.code === 200) {
        // 刷新列表
        await loadKnowledgeBases()
      }
      return response
    } catch (error) {
      console.error('创建知识库失败:', error)
      throw error
    }
  }
  
  // 获取知识库详情
  const loadKnowledgeBaseDetail = async (id: string) => {
    loading.value = true
    try {
      const response = await getKnowledgeBaseDetail(id)
      if (response.code === 200) {
        currentKnowledgeBase.value = response.data
      }
      return response
    } catch (error) {
      console.error('获取知识库详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新知识库
  const updateKB = async (data: UpdateKnowledgeBaseDTO) => {
    try {
      const response = await updateKnowledgeBase(data)
      if (response.code === 200) {
        // 刷新列表
        await loadKnowledgeBases()
      }
      return response
    } catch (error) {
      console.error('更新知识库失败:', error)
      throw error
    }
  }
  
  // 删除知识库
  const deleteKB = async (id: string) => {
    try {
      const response = await deleteKnowledgeBase(id)
      if (response.code === 200) {
        // 刷新列表
        await loadKnowledgeBases()
        // 如果删除的是当前知识库，清空
        if (currentKnowledgeBase.value?.id === id) {
          currentKnowledgeBase.value = null
          currentDocuments.value = []
        }
      }
      return response
    } catch (error) {
      console.error('删除知识库失败:', error)
      throw error
    }
  }
  
  // 加载知识库文档列表
  const loadKnowledgeBaseDocuments = async (knowledgeBaseId: string) => {
    loading.value = true
    try {
      const response = await getKnowledgeBaseDocuments(knowledgeBaseId)
      if (response.code === 200) {
        currentDocuments.value = response.data
      }
      return response
    } catch (error) {
      console.error('加载文档列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 上传文档到知识库
  const uploadDocument = async (file: File, knowledgeBaseId: string, embeddingModelId?: string) => {
    try {
      const response = await uploadDocumentToKnowledgeBase(file, knowledgeBaseId, embeddingModelId)
      if (response.code === 200) {
        // 刷新文档列表
        await loadKnowledgeBaseDocuments(knowledgeBaseId)
      }
      return response
    } catch (error) {
      console.error('上传文档失败:', error)
      throw error
    }
  }
  
  // 删除文档
  const removeDocument = async (documentId: string, knowledgeBaseId?: string) => {
    try {
      const response = await deleteDocument(documentId)
      if (response.code === 200) {
        // 刷新文档列表
        if (knowledgeBaseId) {
          await loadKnowledgeBaseDocuments(knowledgeBaseId)
        }
      }
      return response
    } catch (error) {
      console.error('删除文档失败:', error)
      throw error
    }
  }
  
  // 重新处理文档
  const reprocessDoc = async (documentId: string, knowledgeBaseId?: string) => {
    try {
      const response = await reprocessDocument(documentId)
      if (response.code === 200) {
        // 刷新文档列表
        if (knowledgeBaseId) {
          await loadKnowledgeBaseDocuments(knowledgeBaseId)
        }
      }
      return response
    } catch (error) {
      console.error('重新处理文档失败:', error)
      throw error
    }
  }
  
  return {
    knowledgeBases,
    currentKnowledgeBase,
    currentDocuments,
    loading,
    loadKnowledgeBases,
    createKB,
    loadKnowledgeBaseDetail,
    updateKB,
    deleteKB,
    loadKnowledgeBaseDocuments,
    uploadDocument,
    removeDocument,
    reprocessDoc
  }
})