<template>
  <div class="model-config-management">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
        </svg>
        <h1 class="page-title">模型配置管理</h1>
      </div>
      <div class="header-actions">
        <router-link class="btn btn-secondary" to="/provider-config">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
            <path d="M2 17l10 5 10-5"></path>
            <path d="M2 12l10 5 10-5"></path>
          </svg>
          <span>供应商配置</span>
        </router-link>
        <button class="btn btn-primary" @click="openAddModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>添加模型配置</span>
        </button>
      </div>
    </div>

    <!-- Info Banner -->
    <div class="info-banner">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="16" x2="12" y2="12"></line>
        <line x1="12" y1="8" x2="12.01" y2="8"></line>
      </svg>
      <div class="banner-content">
        <p class="banner-title">两级配置架构</p>
        <p class="banner-text">模型配置管理推理参数（温度、Token上限等），连接信息由关联的供应商配置管理。新架构配置显示供应商配置关联信息。</p>
      </div>
    </div>

    <!-- Model Config List -->
    <div class="config-list-container">
      <div v-if="modelConfigStore.isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="!modelConfigStore.hasModelConfig" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
        </svg>
        <p class="empty-title">暂无模型配置</p>
        <p class="empty-text">点击上方按钮添加新的模型配置</p>
      </div>

      <div v-else class="config-list">
        <div
          v-for="config in modelConfigStore.modelConfigs"
          :key="config.id"
          class="config-card"
          :class="{ 'is-default': config.isDefault === 1 }"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="config-info">
              <h3 class="config-name">{{ config.configName }}</h3>
              <div class="config-meta">
                <span class="model-name">{{ config.modelAlias || config.modelName }}</span>
                <span class="model-type-badge" :class="(config.modelType || 'CHAT') === 'EMBEDDING' ? 'embedding' : 'chat'">
                  {{ (config.modelType || 'CHAT') === 'EMBEDDING' ? '向量模型' : '对话模型' }}
                </span>
                <span class="provider-badge">{{ config.providerName }}</span>
                <span v-if="config.isDefault === 1" class="default-badge">默认</span>
              </div>
            </div>
            <div class="card-actions">
              <button class="action-btn" @click="testConnection(config.id)" :disabled="testingIds.includes(config.id)" title="测试连接">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                <span v-if="testingIds.includes(config.id)">测试中...</span>
                <span v-else>测试</span>
              </button>
              <button class="action-btn danger" @click="handleDelete(config.id)" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <div class="info-row">
              <label class="info-label">供应商配置:</label>
              <span class="info-value provider-config-link">
                <router-link :to="`/provider-config?id=${config.providerConfigId}`">
                  查看关联的供应商配置
                </router-link>
              </span>
            </div>
            
            <!-- 向量模型特有信息 -->
            <div v-if="(config.modelType || 'CHAT') === 'EMBEDDING'" class="embedding-info">
              <div class="info-row">
                <label class="info-label">向量维度:</label>
                <span class="info-value">{{ config.vectorDimensions || '未设置' }}</span>
              </div>
              <div class="info-row">
                <label class="info-label">相似度阈值:</label>
                <span class="info-value">{{ config.similarityThreshold || '未设置' }}</span>
              </div>
            </div>
            
            <!-- 对话模型特有信息 -->
            <div v-if="(config.modelType || 'CHAT') === 'CHAT'" class="chat-info">
              <div class="info-row">
                <label class="info-label">温度参数:</label>
                <span class="info-value">{{ config.temperature }}</span>
              </div>
              <div class="info-row">
                <label class="info-label">最大Token:</label>
                <span class="info-value">{{ config.maxTokens || '未设置' }}</span>
              </div>
              <div class="info-row">
                <label class="info-label">流式输出:</label>
                <span class="info-value" :class="{ 'enabled': config.isStreamingEnabled === 1 }">
                  {{ config.isStreamingEnabled === 1 ? '启用' : '禁用' }}
                </span>
              </div>
            </div>
            
            <div class="info-row">
              <label class="info-label">使用次数:</label>
              <span class="info-value">{{ config.usedCount || 0 }}</span>
            </div>
            <div class="info-row">
              <label class="info-label">创建时间:</label>
              <span class="info-value">{{ formatDate(config.createTime) }}</span>
            </div>
          </div>

          <!-- Card Footer -->
          <div class="card-footer">
            <button v-if="config.isDefault !== 1" class="btn btn-sm btn-secondary" @click="setDefaultConfig(config.id)">
              设为默认
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Modal -->
    <AddModelModal
      v-if="showAddModal"
      @close="closeAddModal"
      @success="onAddSuccess"
    />

    <!-- Test Result Modal -->
    <div v-if="showTestResult" class="modal-overlay" @click.self="showTestResult = false">
      <div class="modal-container small">
        <div class="modal-header">
          <h3 class="modal-title">连接测试结果</h3>
          <button class="close-btn" @click="showTestResult = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="test-result" :class="{ 'success': testResultSuccess, 'error': !testResultSuccess }">
            <svg v-if="testResultSuccess" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            <svg v-else width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="15" y1="9" x2="9" y2="15"></line>
              <line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
            <p class="result-message">{{ testResultMessage }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useModelConfigStore } from '@/stores/modelConfig'
import { useProviderConfigStore } from '@/stores/providerConfig'
import AddModelModal from '@/components/AddModelModal.vue'
import type { AiModelConfigVO } from '@/types/modelConfig'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const modelConfigStore = useModelConfigStore()
const providerConfigStore = useProviderConfigStore()
const toast = useToast()

// Modal state
const showAddModal = ref(false)

// Test state
const testingIds = ref<string[]>([])
const showTestResult = ref(false)
const testResultMessage = ref('')
const testResultSuccess = ref(false)

// Date formatter
const formatDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Modal handlers
const openAddModal = () => {
  showAddModal.value = true
}

const closeAddModal = () => {
  showAddModal.value = false
}

const onAddSuccess = () => {
  closeAddModal()
}

// Delete handler
const handleDelete = async (configId: string) => {
  if (!confirm('确定要删除此模型配置吗？')) {
    return
  }

  try {
    const success = await modelConfigStore.deleteConfig(configId)
    if (!success) {
      toast.showError('删除失败，请稍后重试')
    }
  } catch (error) {
    console.error('删除失败:', error)
    toast.showError('删除失败，请检查网络连接')
  }
}

// Set default handler
const setDefaultConfig = async (configId: string) => {
  try {
    const success = await modelConfigStore.setDefault(configId)
    if (!success) {
      toast.showError('设置默认失败，请稍后重试')
    }
  } catch (error) {
    console.error('设置默认失败:', error)
    toast.showError('设置默认失败，请检查网络连接')
  }
}

// Test connection handler
const testConnection = async (configId: string) => {
  testingIds.value.push(configId)
  
  try {
    const result = await modelConfigStore.testConnection(configId)
    testResultMessage.value = result
    testResultSuccess.value = result.includes('成功') || result.includes('连接正常')
    showTestResult.value = true
  } catch (error) {
    console.error('测试连接失败:', error)
    testResultMessage.value = '测试连接失败'
    testResultSuccess.value = false
    showTestResult.value = true
  } finally {
    testingIds.value = testingIds.value.filter(id => id !== configId)
  }
}

// Initialize
onMounted(async () => {
  await modelConfigStore.init()
  await providerConfigStore.init()
  
  // Check if there's a providerConfigId in the query params
  const providerConfigId = route.query.providerConfigId as string
  if (providerConfigId) {
    // Pre-select the provider config in the add modal
    showAddModal.value = true
  }
})
</script>

<style scoped>
.model-config-management {
  padding: var(--spacing-2xl);
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-icon {
  color: var(--color-primary);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-text);
}

.header-actions {
  display: flex;
  gap: var(--spacing-md);
}

/* Info Banner */
.info-banner {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background-color: var(--color-primary-light);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-xl);
}

.info-banner svg {
  color: var(--color-primary);
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-xs);
}

.banner-text {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

/* Config List Container */
.config-list-container {
  min-height: 400px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--spacing-md);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-icon {
  color: var(--color-text-tertiary);
  margin-bottom: var(--spacing-md);
}

.empty-title {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-sm);
}

.empty-text {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}

/* Config List */
.config-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--spacing-lg);
}

.config-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background-color: var(--color-background);
  transition: all var(--transition-fast);
}

.config-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.config-card.is-default {
  border-color: var(--color-primary);
  background-color: var(--color-primary-light);
}

/* Card Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.config-info {
  flex: 1;
}

.config-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.config-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  align-items: center;
}

.model-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.model-type-badge {
  font-size: var(--font-size-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.model-type-badge.chat {
  background-color: #e3f2fd;
  color: #1976d2;
}

.model-type-badge.embedding {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.provider-badge {
  font-size: var(--font-size-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  background-color: var(--color-background-soft);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
}

.default-badge {
  font-size: var(--font-size-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  background-color: var(--color-primary);
  color: white;
  border-radius: var(--radius-sm);
}

.card-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background-color: var(--color-background);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover:not(:disabled) {
  background-color: var(--color-background-soft);
  color: var(--color-text);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.danger:hover:not(:disabled) {
  border-color: var(--color-danger);
  color: var(--color-danger);
}

/* Card Body */
.card-body {
  padding: var(--spacing-lg);
}

.info-row {
  display: flex;
  align-items: baseline;
  margin-bottom: var(--spacing-md);
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  min-width: 100px;
  flex-shrink: 0;
}

.info-value {
  font-size: var(--font-size-sm);
  color: var(--color-text);
}

.info-value.masked {
  color: var(--color-text-secondary);
}

.info-value.enabled {
  color: var(--color-success);
}

.info-value.provider-config-link a {
  color: var(--color-primary);
  text-decoration: underline;
}

/* Card Footer */
.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

/* Button Styles */
.btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: none;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.btn-secondary {
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-text);
}

.btn-secondary:hover {
  background-color: var(--color-background-soft);
}

.btn-warning {
  background-color: var(--color-warning);
  color: white;
}

.btn-warning:hover {
  background-color: rgba(255, 152, 0, 0.8);
}

.btn-sm {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-xs);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  width: 560px;
  max-width: 90vw;
  max-height: 90vh;
  border-radius: var(--radius-lg);
  background-color: var(--color-background);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-container.small {
  width: 400px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
}

.close-btn {
  padding: var(--spacing-sm);
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.close-btn:hover {
  color: var(--color-text);
}

.modal-body {
  padding: var(--spacing-lg);
  overflow-y: auto;
  flex: 1;
}

/* Test Result */
.test-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl);
  text-align: center;
}

.test-result.success svg {
  color: var(--color-success);
}

.test-result.error svg {
  color: var(--color-danger);
}

.result-message {
  font-size: var(--font-size-lg);
  color: var(--color-text);
  margin-top: var(--spacing-md);
}
</style>