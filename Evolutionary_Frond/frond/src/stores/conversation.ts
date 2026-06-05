// 对话状态管理

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Conversation, Message, ConversationMode, ConversationGroup } from '@/types/conversation'

export const useConversationStore = defineStore('conversation', () => {
  // 状态
  const currentMode = ref<ConversationMode>('quick')
  const conversations = ref<Conversation[]>([])
  const currentConversation = ref<Conversation | null>(null)

  /**
   * 切换对话模式
   */
  const toggleMode = (mode: ConversationMode) => {
    currentMode.value = mode
  }

  /**
   * 创建新对话
   */
  const createConversation = (title: string = '新对话'): Conversation => {
    const newConversation: Conversation = {
      id: generateId(),
      title,
      mode: currentMode.value,
      messages: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    }
    conversations.value.unshift(newConversation)
    currentConversation.value = newConversation
    return newConversation
  }

  /**
   * 选择对话
   */
  const selectConversation = (id: string) => {
    const conversation = conversations.value.find((c) => c.id === id)
    if (conversation) {
      currentConversation.value = conversation
      currentMode.value = conversation.mode
    }
  }

  /**
   * 添加消息到当前对话
   * @param content 消息内容
   * @param role 消息角色
   * @param customId 自定义消息ID（可选，用于流式输出）
   * @param isStreaming 是否正在流式输出（可选）
   */
  const addMessage = (
    content: string,
    role: 'user' | 'assistant',
    customId?: string,
    isStreaming?: boolean,
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
    }

    currentConversation.value.messages.push(message)
    currentConversation.value.updatedAt = new Date()

    // 更新对话标题（使用第一条用户消息）
    if (role === 'user' && currentConversation.value.messages.filter((m) => m.role === 'user').length === 1) {
      currentConversation.value.title = content.slice(0, 20) + (content.length > 20 ? '...' : '')
    }
  }

  /**
   * 删除对话
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
   * 生成唯一 ID
   */
  const generateId = (): string => {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  return {
    currentMode,
    conversations,
    currentConversation,
    groupedConversations,
    toggleMode,
    createConversation,
    selectConversation,
    addMessage,
    deleteConversation,
  }
})