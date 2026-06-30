<template>
  <div class="profile-page">
    <div class="profile-header">
      <h1 class="page-title">个人主页</h1>
      <p class="page-desc">管理您的个人信息和查看使用统计</p>
    </div>

    <div class="profile-content">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h2 class="card-title">基本信息</h2>
        </div>
        <div class="card-body">
          <div class="stats-loading" v-if="isLoadingUserInfo">
            <div class="loading-spinner"></div>
            <span>加载用户信息...</span>
          </div>
          <div class="info-grid" v-else>
            <div class="info-item">
              <div class="info-label">用户名</div>
              <div class="info-value">{{ userInfo?.username || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">邮箱</div>
              <div class="info-value">{{ userInfo?.email || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">真实姓名</div>
              <div class="info-value">{{ userInfo?.realName || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">手机号码</div>
              <div class="info-value">{{ userInfo?.phone || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">性别</div>
              <div class="info-value">{{ genderText }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">状态</div>
              <div class="info-value">
                <span class="status-badge" :class="statusClass">{{ statusText }}</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-label">注册时间</div>
              <div class="info-value">{{ formatDate(userInfo?.createTime) }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">最后登录</div>
              <div class="info-value">{{ formatDate(userInfo?.lastLoginTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 对话统计卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h2 class="card-title">对话统计</h2>
        </div>
        <div class="card-body">
          <div class="stats-loading" v-if="isLoadingStats">
            <div class="loading-spinner"></div>
            <span>加载统计数据...</span>
          </div>
          <div class="stats-grid" v-else>
            <div class="stat-item">
              <div class="stat-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-label">总对话次数</div>
                <div class="stat-value">{{ conversationStats.totalCount || 0 }}</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon today">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-label">今日对话</div>
                <div class="stat-value">{{ conversationStats.todayCount || 0 }}</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon messages">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-label">总消息数</div>
                <div class="stat-value">{{ conversationStats.totalMessages || 0 }}</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon active">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-label">活跃对话</div>
                <div class="stat-value">{{ conversationStats.activeCount || 0 }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Token使用量统计卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h2 class="card-title">Token使用量统计</h2>
        </div>
        <div class="card-body">
          <div class="stats-loading" v-if="isLoadingStats">
            <div class="loading-spinner"></div>
            <span>加载统计数据...</span>
          </div>
          <div class="token-stats" v-else>
            <div class="token-summary">
              <div class="token-total">
                <div class="token-icon">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                  </svg>
                </div>
                <div class="token-info">
                  <div class="token-label">总Token消耗</div>
                  <div class="token-number">{{ formatNumber(tokenStats.totalTokens) }}</div>
                </div>
              </div>
              <div class="token-details">
                <div class="token-detail-item">
                  <div class="detail-label">输入Token</div>
                  <div class="detail-value">{{ formatNumber(tokenStats.inputTokens) }}</div>
                </div>
                <div class="token-detail-item">
                  <div class="detail-label">输出Token</div>
                  <div class="detail-value">{{ formatNumber(tokenStats.outputTokens) }}</div>
                </div>
                <div class="token-detail-item">
                  <div class="detail-label">今日消耗</div>
                  <div class="detail-value">{{ formatNumber(tokenStats.todayTokens) }}</div>
                </div>
              </div>
            </div>

            <!-- Token使用趋势图 -->
            <TokenTrendChart :weekly-trend="tokenStats.weeklyTrend" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { get } from '@/utils/request'
import TokenTrendChart from '@/components/TokenTrendChart.vue'
import type { ConversationStatsVO, ProfileStatsVO, TokenStatsVO } from '@/types/profile'
import type { User } from '@/types/user'

const userStore = useUserStore()

// 用户信息（从后端重新加载）
const userInfo = ref<User | null>(null)
const isLoadingUserInfo = ref(false)

// 统计数据
const conversationStats = ref<ConversationStatsVO>({
  totalCount: 0,
  todayCount: 0,
  totalMessages: 0,
  activeCount: 0
})

const tokenStats = ref<TokenStatsVO>({
  totalTokens: 0,
  inputTokens: 0,
  outputTokens: 0,
  todayTokens: 0,
  weeklyTrend: []
})

const isLoadingStats = ref(false)

// 加载用户信息
const loadUserInfo = async () => {
  isLoadingUserInfo.value = true
  try {
    const response = await get<User>('/profile/user-info')
    if (response.code === 200 && response.data) {
      userInfo.value = response.data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 如果失败，尝试从 store 获取
    userInfo.value = userStore.userInfo
  } finally {
    isLoadingUserInfo.value = false
  }
}

// 性别文本
const genderText = computed(() => {
  if (!userInfo.value?.gender) return '未设置'
  return userInfo.value.gender === 1 ? '男' : userInfo.value.gender === 2 ? '女' : '未设置'
})

// 状态文本
const statusText = computed(() => {
  if (!userInfo.value?.status) return '未知'
  return userInfo.value.status === 1 ? '正常' : userInfo.value.status === 0 ? '禁用' : '未知'
})

// 状态样式类
const statusClass = computed(() => {
  if (!userInfo.value?.status) return ''
  return userInfo.value.status === 1 ? 'status-active' : 'status-disabled'
})

// 格式化日期
const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  try {
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (error) {
    return '-'
  }
}

// 格式化数字
const formatNumber = (num: number | undefined) => {
  if (!num) return '0'
  if (num >= 1000000) {
    return `${(num / 1000000).toFixed(2)}M`
  } else if (num >= 1000) {
    return `${(num / 1000).toFixed(2)}K`
  }
  return num.toString()
}

// 加载统计数据
const loadStats = async () => {
  isLoadingStats.value = true
  try {
    const response = await get<ProfileStatsVO>('/profile/stats')
    if (response.code === 200 && response.data) {
      conversationStats.value = response.data.conversationStats || {
        totalCount: 0,
        todayCount: 0,
        totalMessages: 0,
        activeCount: 0
      }
      tokenStats.value = response.data.tokenStats || {
        totalTokens: 0,
        inputTokens: 0,
        outputTokens: 0,
        todayTokens: 0,
        weeklyTrend: []
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 发生错误时使用默认数据
    conversationStats.value = {
      totalCount: 0,
      todayCount: 0,
      totalMessages: 0,
      activeCount: 0
    }
    tokenStats.value = {
      totalTokens: 0,
      inputTokens: 0,
      outputTokens: 0,
      todayTokens: 0,
      weeklyTrend: []
    }
  } finally {
    isLoadingStats.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadStats()
})
</script>

<style scoped>
.profile-page {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.profile-content {
  display: grid;
  gap: 24px;
}

.profile-card {
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.card-body {
  padding: 24px;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 15px;
  color: var(--color-text);
  font-weight: 600;
}

.status-badge {
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
}

.status-active {
  background-color: var(--color-success-light);
  color: var(--color-success);
}

.status-disabled {
  background-color: var(--color-error-light);
  color: var(--color-error);
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background-color: var(--color-background-soft);
  border-radius: var(--radius-md);
  transition: all 0.2s ease-out;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.today {
  background-color: var(--color-success-light);
  color: var(--color-success);
}

.stat-icon.messages {
  background-color: var(--color-warning-light);
  color: var(--color-warning);
}

.stat-icon.active {
  background-color: var(--color-info-light);
  color: var(--color-info);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
}

/* Token统计 */
.token-stats {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.token-summary {
  display: flex;
  gap: 32px;
}

.token-total {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, #e8f4ff 100%);
  border-radius: var(--radius-lg);
}

.token-icon {
  color: var(--color-primary);
}

.token-info {
  flex: 1;
}

.token-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.token-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-primary);
}

.token-details {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.token-detail-item {
  padding: 16px;
  background-color: var(--color-background-soft);
  border-radius: var(--radius-md);
}

.detail-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.detail-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

/* 加载状态 */
.stats-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 40px;
  color: var(--color-text-secondary);
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .token-summary {
    flex-direction: column;
  }

  .token-details {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 24px 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .token-details {
    grid-template-columns: 1fr;
  }
}
</style>