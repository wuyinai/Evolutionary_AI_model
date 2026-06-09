// 对话管理相关API接口

import { get, del } from '@/utils/request'
import type { ApiConversation, ApiConversationMessage } from '@/types/conversation'
import type { ApiResponse } from '@/types/api'

/**
 * 获取用户会话列表
 * @returns 会话列表
 */
export const getUserConversations = async (): Promise<ApiConversation[]> => {
  const response = await get<ApiConversation[]>('/chat/conversations')
  return response.data || []
}

/**
 * 获取会话消息历史
 * @param conversationId 会话ID
 * @returns 消息历史列表
 */
export const getConversationMessages = async (
  conversationId: string,
): Promise<ApiConversationMessage[]> => {
  const response = await get<ApiConversationMessage[]>(`/chat/messages/${conversationId}`)
  return response.data || []
}

/**
 * 删除会话（逻辑删除）
 * @param conversationId 会话ID
 * @returns 删除结果
 */
export const deleteConversation = async (conversationId: string): Promise<ApiResponse<void>> => {
  return await del<void>(`/chat/conversations/${conversationId}`)
}