<template>
  <div class="token-trend">
    <div class="trend-header">
      <h3 class="trend-title">近7天Token使用趋势</h3>
    </div>
    <div class="trend-chart" v-if="weeklyTrend && weeklyTrend.length > 0">
      <div class="chart-bars">
        <div
          v-for="(item, index) in weeklyTrend"
          :key="index"
          class="chart-bar"
          :style="{ height: calculateBarHeight(item.tokens) }"
        >
          <div class="bar-label">{{ item.date }}</div>
          <div class="bar-value">{{ formatNumber(item.tokens) }}</div>
        </div>
      </div>
    </div>
    <div class="trend-empty" v-else>
      <span>暂无趋势数据</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DailyTokenUsageVO } from '@/types/profile'

const props = defineProps<{
  weeklyTrend: DailyTokenUsageVO[]
}>()

// 计算柱状图最大高度
const maxWeeklyTokens = computed(() => {
  const trend = props.weeklyTrend
  if (!trend || trend.length === 0) return 100
  return Math.max(...trend.map(item => item.tokens || 0))
})

// 计算柱状图高度
const calculateBarHeight = (tokens: number) => {
  if (!tokens || !maxWeeklyTokens.value) return '0%'
  const height = (tokens / maxWeeklyTokens.value) * 100
  return `${Math.max(height, 5)}%` // 最小高度5%
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
</script>

<style scoped>
.token-trend {
  padding: 24px;
  background-color: var(--color-background-soft);
  border-radius: var(--radius-lg);
}

.trend-header {
  margin-bottom: 20px;
}

.trend-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.trend-chart {
  height: 200px;
  display: flex;
  align-items: flex-end;
}

.chart-bars {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  width: 100%;
  height: 100%;
  gap: 16px;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  transition: all 0.3s ease-out;
  position: relative;
  min-height: 20px;
  cursor: pointer;
}

.chart-bar:hover {
  background: linear-gradient(180deg, var(--color-primary-hover) 0%, var(--color-primary) 100%);
  transform: scaleY(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.bar-label {
  position: absolute;
  bottom: -30px;
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.bar-value {
  position: absolute;
  top: -30px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text);
}

.trend-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

@media (max-width: 768px) {
  .chart-bars {
    gap: 8px;
  }

  .bar-label {
    font-size: 11px;
  }

  .bar-value {
    font-size: 11px;
    top: -24px;
  }
}
</style>
