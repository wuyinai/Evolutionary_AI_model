// 对话状态管理

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Conversation, Message, ConversationGroup, ApiConversation, ApiConversationMessage, DocumentChunk } from '@/types/conversation'
import { getUserConversations, getConversationMessages, deleteConversation as deleteConversationApi } from '@/utils/conversationApi'

export const useConversationStore = defineStore('conversation', () => {
  // 状态
  const conversations = ref<Conversation[]>([])
  const currentConversation = ref<Conversation | null>(null)
  const isLoadingConversations = ref(false) // 加载会话列表状态
  const isLoadingMessages = ref(false) // 加载消息历史状态
  const selectedRoleId = ref<string | null>(null) // 当前选中的AI角色ID
  const selectedRoleName = ref<string>('未选择角色') // 当前选中的AI角色名称

  /**
   * 从后端加载用户会话列表
   */
  const loadConversations = async () => {
    isLoadingConversations.value = true
    try {
      const apiConversations = await getUserConversations()
      // 转换后端数据格式为前端格式
      conversations.value = apiConversations.map(convertApiConversationToLocal)
    } catch (error) {
      console.error('加载会话列表失败:', error)
      conversations.value = []
    } finally {
      isLoadingConversations.value = false
    }
  }

  /**
   * 将后端会话数据转换为前端格式
   */
  const convertApiConversationToLocal = (apiConv: ApiConversation): Conversation => {
    return {
      id: apiConv.conversationId, // 使用 conversationId 作为前端会话ID
      title: apiConv.title || '新对话',
      messages: [], // 消息列表初始为空，点击时再加载
      pinnedConfigId: apiConv.pinnedConfigId,
      createdAt: new Date(apiConv.createTime),
      updatedAt: new Date(apiConv.updateTime || apiConv.lastMessageTime || apiConv.createTime),
    }
  }

  /**
   * 将后端消息数据转换为前端格式
   */
  const convertApiMessageToLocal = (apiMsg: ApiConversationMessage): Message => {
    // 解析文档块JSON字符串
    let documentChunks: Message['documentChunks'] = undefined
    if (apiMsg.documentChunks) {
      try {
        documentChunks = JSON.parse(apiMsg.documentChunks) as DocumentChunk[]
      } catch (e) {
        console.error('解析文档块信息失败:', e)
      }
    }
    
    return {
      id: apiMsg.messageId,
      role: apiMsg.role.toLowerCase() === 'user' ? 'user' : 'assistant',
      content: apiMsg.content,
      timestamp: new Date(apiMsg.createTime),
      isStreaming: false,
      documentChunks,
    }
  }

  /**
   * 选择对话并加载其消息历史
   */
  const selectConversation = async (id: string) => {
    // 先设置当前会话（不等待消息加载）
    const conversation = conversations.value.find((c) => c.id === id)
    if (conversation) {
      currentConversation.value = conversation
    }

    // 如果该会话的消息列表为空，则从后端加载
    if (conversation && conversation.messages.length === 0) {
      isLoadingMessages.value = true
      try {
        const apiMessages = await getConversationMessages(id)
        // 转换并设置消息列表
        conversation.messages = apiMessages.map(convertApiMessageToLocal)
      } catch (error) {
        console.error('加载会话消息失败:', error)
        conversation.messages = []
      } finally {
        isLoadingMessages.value = false
      }
    }
  }

  /**
   * 创建新对话（本地创建，等待发送第一条消息时后端会创建）
   * 如果已存在未使用的"新对话"（没有消息的对话），则切换到该对话而不创建新的
   */
  const createConversation = (title: string = '新对话', pinnedConfigId?: string): Conversation => {
    // 检查是否已存在未使用的"新对话"（没有消息的对话）
    const existingEmptyConversation = conversations.value.find(
      (conv) => conv.messages.length === 0 && conv.title === '新对话'
    )

    if (existingEmptyConversation) {
      // 如果存在未使用的"新对话"，直接切换到该对话
      currentConversation.value = existingEmptyConversation
      return existingEmptyConversation
    }

    // 否则创建新的对话
    const newConversation: Conversation = {
      id: generateId(),
      title,
      messages: [],
      pinnedConfigId,
      createdAt: new Date(),
      updatedAt: new Date(),
    }
    conversations.value.unshift(newConversation)
    currentConversation.value = newConversation
    return newConversation
  }

  /**
   * 添加消息到当前对话
   * @param content 消息内容
   * @param role 消息角色
   * @param customId 自定义消息ID（可选，用于流式输出）
   * @param isStreaming 是否正在流式输出（可选）
   * @param documentChunks 相关的文档块信息（可选）
   */
  const addMessage = (
    content: string,
    role: 'user' | 'assistant',
    customId?: string,
    isStreaming?: boolean,
    documentChunks?: Message['documentChunks'],
  ) => {
    if (!currentConversation.value) {
      currentConversation.value = createConversation()
    }

    const message: Message = {
      id: customId || generateId(),
      role,
      content,
      timestamp: new Date(),
      isStreaming: isStreaming || false,
      documentChunks,
    }

    currentConversation.value.messages.push(message)
    currentConversation.value.updatedAt = new Date()

    // 更新对话标题（使用第一条用户消息）
    if (role === 'user' && currentConversation.value.messages.filter((m) => m.role === 'user').length === 1) {
      currentConversation.value.title = content.slice(0, 20) + (content.length > 20 ? '...' : '')
    }
  }

  /**
   * 删除对话（调用后端API进行逻辑删除）
   */
  const deleteConversationFromBackend = async (id: string) => {
    try {
      // 调用后端API删除会话
      await deleteConversationApi(id)
      
      // 从本地列表中移除
      const index = conversations.value.findIndex((c) => c.id === id)
      if (index !== -1) {
        conversations.value.splice(index, 1)
        if (currentConversation.value?.id === id) {
          currentConversation.value = conversations.value[0] || null
        }
      }
      
      console.log('会话删除成功，会话ID:', id)
    } catch (error) {
      console.error('删除会话失败:', error)
      throw error
    }
  }

  /**
   * 删除对话（本地删除，不调用后端API）
   */
  const deleteConversation = (id: string) => {
    const index = conversations.value.findIndex((c) => c.id === id)
    if (index !== -1) {
      conversations.value.splice(index, 1)
      if (currentConversation.value?.id === id) {
        currentConversation.value = conversations.value[0] || null
      }
    }
  }

  /**
   * 钉选模型到当前对话
   */
  const pinModelToConversation = (configId: string) => {
    if (currentConversation.value) {
      currentConversation.value.pinnedConfigId = configId
      currentConversation.value.updatedAt = new Date()
    }
  }

  /**
   * 取消钉选模型
   */
  const unpinModel = () => {
    if (currentConversation.value) {
      currentConversation.value.pinnedConfigId = undefined
      currentConversation.value.updatedAt = new Date()
    }
  }

  /**
   * 获取当前对话钉选的模型配置ID
   */
  const getPinnedConfigId = (): string | undefined => {
    return currentConversation.value?.pinnedConfigId
  }

  /**
   * 更新对话标题
   */
  const updateTitle = (id: string, title: string) => {
    const conversation = conversations.value.find((c) => c.id === id)
    if (conversation) {
      conversation.title = title
      conversation.updatedAt = new Date()
    }
  }

  /**
   * 按日期分组对话
   */
  const groupedConversations = computed<ConversationGroup[]>(() => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)

    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)

    const weekAgo = new Date(today)
    weekAgo.setDate(weekAgo.getDate() - 7)

    const groups: { [key: string]: Conversation[] } = {
      今天: [],
      昨天: [],
      '7天内': [],
      更早: [],
    }

    conversations.value.forEach((conversation) => {
      const convDate = new Date(conversation.updatedAt)
      convDate.setHours(0, 0, 0, 0)

      if (convDate.getTime() === today.getTime()) {
        groups['今天'].push(conversation)
      } else if (convDate.getTime() === yesterday.getTime()) {
        groups['昨天'].push(conversation)
      } else if (convDate.getTime() >= weekAgo.getTime()) {
        groups['7天内'].push(conversation)
      } else {
        groups['更早'].push(conversation)
      }
    })

    return Object.entries(groups)
      .filter(([, conversations]) => conversations.length > 0)
      .map(([label, conversations]) => ({ label, conversations }))
  })

  /**
   * 设置选中的AI角色
   */
  const setSelectedRole = (roleId: string | null, roleName: string) => {
    selectedRoleId.value = roleId
    selectedRoleName.value = roleName
  }

  /**
   * 清除选中的AI角色（恢复默认）
   */
  const clearSelectedRole = () => {
    selectedRoleId.value = null
    selectedRoleName.value = '默认角色'
  }

  /**
   * 生成唯一 ID
   */
  const generateId = (): string => {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  return {
    conversations,
    currentConversation,
    groupedConversations,
    isLoadingConversations,
    isLoadingMessages,
    selectedRoleId,
    selectedRoleName,
    loadConversations,
    selectConversation,
    createConversation,
    addMessage,
    deleteConversation,
    deleteConversationFromBackend,
    pinModelToConversation,
    unpinModel,
    getPinnedConfigId,
    updateTitle,
    setSelectedRole,
    clearSelectedRole,
  }
})