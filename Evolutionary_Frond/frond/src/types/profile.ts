// 个人主页相关类型定义

/** 每日Token使用量 */
export interface DailyTokenUsageVO {
  date: string
  tokens: number
}

/** Token统计 */
export interface TokenStatsVO {
  totalTokens: number
  inputTokens: number
  outputTokens: number
  todayTokens: number
  weeklyTrend: DailyTokenUsageVO[]
}

/** 对话统计 */
export interface ConversationStatsVO {
  totalCount: number
  todayCount: number
  totalMessages: number
  activeCount: number
}

/** 个人主页统计总VO */
export interface ProfileStatsVO {
  conversationStats: ConversationStatsVO
  tokenStats: TokenStatsVO
}
