<template>
  <div class="agent-view">
    <!-- 左侧配置区域 -->
    <div class="config-panel">
      <div class="panel-header">
        <h2>Agent助手</h2>
        <p class="description">配置任务参数并执行Agent任务</p>
      </div>

      <!-- 任务输入 -->
      <div class="config-section">
        <label class="section-label">任务描述</label>
        <textarea
          v-model="agentStore.currentTask"
          class="task-input"
          placeholder="请输入要执行的任务，例如：计算 25 * 47 + 18"
          rows="4"
          :disabled="agentStore.isExecuting"
        ></textarea>
      </div>

      <!-- 工具选择 -->
      <div class="config-section">
        <label class="section-label">可用工具</label>
        <div v-if="agentStore.loadingTools" class="loading-tools">
          <div class="spinner"></div>
          <span>加载工具列表...</span>
        </div>
        <div v-else-if="agentStore.availableTools.length === 0" class="empty-tools">
          <span>暂无可用工具</span>
        </div>
        <div v-else class="tools-grid">
          <div
            v-for="tool in agentStore.availableTools"
            :key="tool.name"
            class="tool-card"
            :class="{ selected: agentStore.selectedTools.includes(tool.name) }"
            @click.stop="agentStore.toggleTool(tool.name)"
          >
            <div class="tool-header">
              <svg class="tool-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path>
              </svg>
              <span class="tool-name">{{ tool.name }}</span>
            </div>
            <p class="tool-description">{{ tool.description }}</p>
            <div v-if="agentStore.selectedTools.includes(tool.name)" class="selected-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 模型选择 -->
      <div class="config-section">
        <label class="section-label">模型配置</label>
        <ModelSelector @change="handleModelChange" :disabled="agentStore.isExecuting" />
      </div>

      <!-- 最大执行步骤 -->
      <div class="config-section">
        <label class="section-label">最大执行步骤</label>
        <input
          v-model.number="agentStore.maxSteps"
          type="number"
          class="steps-input"
          min="1"
          max="20"
          :disabled="agentStore.isExecuting"
        />
      </div>

      <!-- 执行按钮 -->
      <div class="action-buttons">
        <button
          class="execute-btn streaming"
          @click="handleExecuteStreaming"
          :disabled="agentStore.isExecuting || !agentStore.currentTask.trim()"
        >
          <svg v-if="!agentStore.isExecuting" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="5 3 19 12 5 21 5 3"></polygon>
          </svg>
          <div v-else class="btn-spinner"></div>
          <span>{{ agentStore.isExecuting ? '执行中...' : '流式执行' }}</span>
        </button>
        <button
          class="execute-btn sync"
          @click="handleExecuteSync"
          :disabled="agentStore.isExecuting || !agentStore.currentTask.trim()"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          <span>同步执行</span>
        </button>
        <button
          class="clear-btn"
          @click="agentStore.clearResult"
          :disabled="agentStore.isExecuting"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 6h18"></path>
            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
          </svg>
          <span>清空结果</span>
        </button>
      </div>
    </div>

    <!-- 右侧结果区域 -->
    <div class="result-panel">
      <!-- 错误提示 -->
      <div v-if="agentStore.hasError" class="error-banner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
        <span>{{ agentStore.error }}</span>
      </div>

      <!-- 执行状态指示器 -->
      <div v-if="agentStore.isExecuting" class="status-indicator">
        <div class="status-spinner"></div>
        <span>Agent正在执行任务...</span>
      </div>

      <!-- 流式输出内容 -->
      <div v-if="agentStore.streamingContent" class="result-section">
        <div class="section-header">
          <h3>执行结果</h3>
          <span class="status-badge" :class="getStatusClass()">
            {{ getStatusText() }}
          </span>
        </div>
        <div class="streaming-content">
          <pre>{{ agentStore.streamingContent }}</pre>
        </div>
      </div>

      <!-- 同步执行结果 -->
      <div v-if="agentStore.executionResult && !agentStore.streamingContent" class="result-section">
        <div class="section-header">
          <h3>执行结果</h3>
          <span class="status-badge" :class="getStatusClass()">
            {{ getStatusText() }}
          </span>
        </div>
        <div class="result-content">
          <div class="result-item">
            <label>任务ID:</label>
            <span>{{ agentStore.executionResult.taskId }}</span>
          </div>
          <div class="result-item">
            <label>任务描述:</label>
            <span>{{ agentStore.executionResult.task }}</span>
          </div>
          <div class="result-item">
            <label>执行结果:</label>
            <div class="result-text">{{ agentStore.executionResult.result }}</div>
          </div>
          <div v-if="agentStore.executionResult.duration" class="result-item">
            <label>执行时长:</label>
            <span>{{ agentStore.executionResult.duration }}ms</span>
          </div>
        </div>
      </div>

      <!-- 执行日志 -->
      <div v-if="agentStore.executionResult?.executionLogs?.length" class="logs-section">
        <div class="section-header">
          <h3>执行日志</h3>
          <span class="log-count">{{ agentStore.executionResult.executionLogs.length }} 步骤</span>
        </div>
        <div class="execution-logs">
          <div
            v-for="(log, index) in agentStore.executionResult.executionLogs"
            :key="index"
            class="log-item"
          >
            <div class="log-header">
              <span class="log-step">步骤 {{ log.step }}</span>
            </div>
            <div class="log-content">
              <div class="log-thought">
                <label>思考:</label>
                <p>{{ log.thought }}</p>
              </div>
              <div class="log-action">
                <label>行动:</label>
                <p>{{ log.action }}</p>
              </div>
              <div class="log-input">
                <label>输入:</label>
                <p>{{ log.actionInput }}</p>
              </div>
              <div class="log-observation">
                <label>观察:</label>
                <p>{{ log.observation }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!agentStore.hasResult && !agentStore.streamingContent && !agentStore.isExecuting" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <circle cx="12" cy="12" r="10"></circle>
          <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>
        <h3>准备执行任务</h3>
        <p>在左侧输入任务描述，选择工具和模型，然后点击执行按钮</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAgentStore } from '@/stores/agent'
import ModelSelector from '@/components/ModelSelector.vue'

const agentStore = useAgentStore()

// 处理模型选择变化
const handleModelChange = (configId: string | null) => {
  agentStore.setConfigId(configId ? parseInt(configId) : null)
}

// 流式执行
const handleExecuteStreaming = () => {
  agentStore.executeStreaming()
}

// 同步执行
const handleExecuteSync = () => {
  agentStore.executeSync()
}

// 获取状态样式类
const getStatusClass = () => {
  const status = agentStore.executionStatus
  if (status === 'RUNNING') return 'running'
  if (status === 'COMPLETED') return 'completed'
  if (status === 'FAILED') return 'failed'
  return ''
}

// 获取状态文本
const getStatusText = () => {
  const status = agentStore.executionStatus
  if (status === 'RUNNING') return '执行中'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'FAILED') return '失败'
  return '空闲'
}

// 组件挂载时加载工具列表
onMounted(() => {
  agentStore.loadAvailableTools()
})
</script>

<style scoped>
.agent-view {
  display: flex;
  width: 100%;
  height: 100vh;
  background-color: #ffffff;
}

/* 左侧配置面板 */
.config-panel {
  width: 400px;
  min-width: 400px;
  height: 100vh;
  overflow-y: auto;
  background-color: #fafafa;
  border-right: 1px solid var(--color-border);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px 0;
}

.panel-header .description {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.config-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.task-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  transition: border-color 0.2s;
  font-family: inherit;
}

.task-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.task-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

/* 工具选择 */
.loading-tools {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px;
  color: var(--color-text-secondary);
}

.empty-tools {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  /* 确保网格容器不会捕获点击事件 */
  pointer-events: none;
}

.tool-card {
  position: relative;
  padding: 12px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
  /* 确保工具卡片可以接收点击事件 */
  pointer-events: auto;
  /* 防止事件冒泡 */
  user-select: none;
}

.tool-card:hover {
  border-color: var(--color-primary);
  background-color: var(--color-primary-light);
}

.tool-card.selected {
  border-color: var(--color-primary);
  background-color: var(--color-primary-light);
}

.tool-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.tool-icon {
  color: var(--color-primary);
}

.tool-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.tool-description {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.selected-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background-color: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

/* 模型选择器 */
.config-section :deep(.model-selector) {
  width: 100%;
}

.config-section :deep(.selector-btn) {
  width: 100%;
  justify-content: space-between;
}

.config-section :deep(.dropdown-menu) {
  bottom: auto;
  top: calc(100% + 4px);
}

/* 最大执行步骤 */
.steps-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  transition: border-color 0.2s;
}

.steps-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.steps-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

/* 执行按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.execute-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.execute-btn.streaming {
  background-color: var(--color-primary);
  color: white;
}

.execute-btn.streaming:hover:not(:disabled) {
  background-color: #0066cc;
}

.execute-btn.sync {
  background-color: #ffffff;
  border: 1px solid var(--color-primary);
  color: var(--color-primary);
}

.execute-btn.sync:hover:not(:disabled) {
  background-color: var(--color-primary-light);
}

.execute-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background-color: #ffffff;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover:not(:disabled) {
  background-color: var(--color-background-soft);
  border-color: var(--color-text-secondary);
}

.clear-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 右侧结果面板 */
.result-panel {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 错误提示 */
.error-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #fff5f5;
  border: 1px solid #fc8181;
  border-radius: var(--radius-md);
  color: #c53030;
}

/* 执行状态指示器 */
.status-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: var(--color-primary-light);
  border-radius: var(--radius-md);
  color: var(--color-primary);
  font-weight: 500;
}

.status-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-primary);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 结果区域 */
.result-section,
.logs-section {
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background-color: #fafafa;
  border-bottom: 1px solid var(--color-border);
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.status-badge.running {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge.completed {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.failed {
  background-color: #f8d7da;
  color: #721c24;
}

.log-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 流式输出内容 */
.streaming-content {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.streaming-content pre {
  margin: 0;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: var(--radius-md);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
}

/* 同步执行结果 */
.result-content {
  padding: 20px;
}

.result-item {
  margin-bottom: 16px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.result-item span {
  font-size: 14px;
  color: var(--color-text);
}

.result-text {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 执行日志 */
.execution-logs {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.log-item {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.log-header {
  padding: 12px 16px;
  background-color: #fafafa;
  border-bottom: 1px solid var(--color-border);
}

.log-step {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}

.log-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-thought,
.log-action,
.log-input,
.log-observation {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.log-thought label,
.log-action label,
.log-input label,
.log-observation label {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.log-thought p,
.log-action p,
.log-input p,
.log-observation p {
  margin: 0;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: var(--radius-sm);
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--color-text-secondary);
}

.empty-state svg {
  color: #d1d5db;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.empty-state p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
  text-align: center;
  max-width: 300px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .agent-view {
    flex-direction: column;
  }

  .config-panel {
    width: 100%;
    min-width: auto;
    height: auto;
    max-height: 50vh;
    border-right: none;
    border-bottom: 1px solid var(--color-border);
  }

  .result-panel {
    height: auto;
    flex: 1;
  }
}

@media (max-width: 768px) {
  .config-panel {
    padding: 16px;
  }

  .result-panel {
    padding: 16px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>