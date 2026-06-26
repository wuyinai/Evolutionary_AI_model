<template>
  <div class="provider-config-management">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
          <path d="M2 17l10 5 10-5"></path>
          <path d="M2 12l10 5 10-5"></path>
        </svg>
        <h1 class="page-title">供应商配置管理</h1>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openAddModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>添加供应商配置</span>
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
        <p class="banner-title">两级配置架构说明</p>
        <p class="banner-text">供应商配置管理连接信息（API密钥、端点地址、协议类型），模型配置管理推理参数（温度、Token上限等）。建议先创建供应商配置，再创建模型配置进行关联。</p>
      </div>
    </div>

    <!-- Provider Config List -->
    <div class="config-list-container">
      <div v-if="providerConfigStore.isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="!providerConfigStore.hasProviderConfig" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
          <path d="M2 17l10 5 10-5"></path>
          <path d="M2 12l10 5 10-5"></path>
        </svg>
        <p class="empty-title">暂无供应商配置</p>
        <p class="empty-text">点击上方按钮添加新的供应商配置</p>
      </div>

      <div v-else class="config-list">
        <div
          v-for="config in providerConfigStore.providerConfigs"
          :key="config.id"
          class="config-card"
          :class="{ 'is-default': config.isDefault === 1 }"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="config-info">
              <h3 class="config-name">{{ config.configName }}</h3>
              <div class="config-meta">
                <span class="provider-name">{{ config.providerName }}</span>
                <span class="protocol-badge">{{ getProtocolDisplayName(config.protocolType) }}</span>
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
              <button class="action-btn" @click="openEditModal(config)" title="编辑">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
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
              <label class="info-label">API端点:</label>
              <span class="info-value">{{ config.apiEndpoint }}</span>
            </div>
            <div class="info-row">
              <label class="info-label">API密钥:</label>
              <span class="info-value masked">{{ config.apiKeyMasked }}</span>
            </div>
            <div class="info-row">
              <label class="info-label">状态:</label>
              <span class="info-value status" :class="{ 'active': config.status === 1, 'inactive': config.status === 0 }">
                {{ config.status === 1 ? '启用' : '禁用' }}
              </span>
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
            <router-link class="btn btn-sm btn-primary" :to="`/model-config?providerConfigId=${config.id}`">
              创建模型配置
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">{{ isEditMode ? '编辑供应商配置' : '添加供应商配置' }}</h3>
          <button class="close-btn" @click="closeModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <!-- Provider Selection -->
            <div class="form-group">
              <label class="form-label">供应商</label>
              <select v-model="form.providerCode" class="form-select" required @change="onProviderChange" :disabled="isEditMode">
                <option value="">请选择供应商</option>
                <option v-for="provider in modelConfigStore.providers" :key="provider.providerCode" :value="provider.providerCode">
                  {{ provider.providerName }}
                </option>
              </select>
            </div>

            <!-- Protocol Type -->
            <div class="form-group">
              <label class="form-label">协议类型</label>
              <select v-model="form.protocolType" class="form-select" required :disabled="isEditMode">
                <option value="">请选择协议类型</option>
                <option value="OPENAI">OpenAI协议</option>
                <option value="ANTHROPIC">Anthropic协议</option>
                <option value="OLLAMA">Ollama协议</option>
                <option value="AZURE_OPENAI">Azure OpenAI协议</option>
                <option value="QWEN">通义千问协议</option>
                <option value="ERNIE">文心一言协议</option>
                <option value="DEEPSEEK">DeepSeek协议</option>
                <option value="CUSTOM">自定义协议</option>
              </select>
              <p v-if="form.protocolType" class="form-tip">{{ getProtocolDescription(form.protocolType) }}</p>
            </div>

            <!-- Config Name -->
            <div class="form-group">
              <label class="form-label">配置名称</label>
              <input
                v-model="form.configName"
                type="text"
                class="form-input"
                placeholder="例如：我的DeepSeek配置"
                required
              />
            </div>

            <!-- API Key -->
            <div class="form-group">
              <label class="form-label">API密钥</label>
              <input
                v-model="form.apiKey"
                type="password"
                class="form-input"
                :placeholder="getApiKeyPlaceholder(form.protocolType)"
                required
              />
              <p v-if="form.protocolType === 'OLLAMA'" class="form-tip">Ollama本地服务无需API密钥，可留空或填写任意值</p>
            </div>

            <!-- API Endpoint -->
            <div class="form-group">
              <label class="form-label">API端点</label>
              <input
                v-model="form.apiEndpoint"
                type="text"
                class="form-input"
                :placeholder="getEndpointPlaceholder(form.protocolType)"
              />
              <p class="form-tip">留空将使用协议默认端点</p>
            </div>

            <!-- Extra Config (for special protocols) -->
            <div v-if="needsExtraConfig(form.protocolType)" class="form-group">
              <label class="form-label">扩展配置（JSON格式）</label>
              <textarea
                v-model="form.extraConfig"
                class="form-textarea"
                rows="3"
                placeholder='{"deploymentName": "my-deployment", "apiVersion": "2024-02-01"}'
              ></textarea>
              <p class="form-tip">{{ getExtraConfigTip(form.protocolType) }}</p>
            </div>

            <!-- Timeout & Retries -->
            <div class="form-row">
              <div class="form-group half">
                <label class="form-label">超时时间（秒）</label>
                <input
                  v-model.number="form.timeoutSeconds"
                  type="number"
                  class="form-input"
                  placeholder="30"
                  min="5"
                  max="300"
                />
              </div>
              <div class="form-group half">
                <label class="form-label">最大重试次数</label>
                <input
                  v-model.number="form.maxRetries"
                  type="number"
                  class="form-input"
                  placeholder="3"
                  min="0"
                  max="10"
                />
              </div>
            </div>

            <!-- Is Default -->
            <div class="form-group checkbox-group">
              <label class="checkbox-label">
                <input v-model="form.isDefault" type="checkbox" :true-value="1" :false-value="0" />
                <span>设为默认供应商配置</span>
              </label>
            </div>

            <!-- Remark -->
            <div class="form-group">
              <label class="form-label">备注</label>
              <textarea
                v-model="form.remark"
                class="form-textarea"
                rows="2"
                placeholder="可选备注信息"
              ></textarea>
            </div>
          </form>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
            <span v-if="isSubmitting">{{ isEditMode ? '更新中...' : '添加中...' }}</span>
            <span v-else>{{ isEditMode ? '更新' : '添加' }}</span>
          </button>
        </div>
      </div>
    </div>

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
import { useProviderConfigStore } from '@/stores/providerConfig'
import { useModelConfigStore } from '@/stores/modelConfig'
import { useToast } from '@/composables/useToast'
import type { AiProviderConfigVO, AiProviderConfigAddDTO, AiProviderConfigUpdateDTO, ProtocolType } from '@/types/providerConfig'
import { PROTOCOL_CONFIG_TIPS } from '@/types/providerConfig'

const providerConfigStore = useProviderConfigStore()
const modelConfigStore = useModelConfigStore()
const toast = useToast()

// Modal state
const showModal = ref(false)
const isEditMode = ref(false)
const isSubmitting = ref(false)
const editingConfigId = ref<string | null>(null)

// Test state
const testingIds = ref<string[]>([])
const showTestResult = ref(false)
const testResultMessage = ref('')
const testResultSuccess = ref(false)

// Form data
const form = ref<AiProviderConfigAddDTO & { id?: string }>({
  configName: '',
  providerCode: '',
  protocolType: 'OPENAI',
  apiKey: '',
  apiEndpoint: '',
  extraConfig: '',
  isDefault: 0,
  timeoutSeconds: 30,
  maxRetries: 3,
  remark: '',
})

// Protocol type helpers
const getProtocolDisplayName = (protocolType: ProtocolType): string => {
  return PROTOCOL_CONFIG_TIPS[protocolType]?.displayName || protocolType
}

const getProtocolDescription = (protocolType: ProtocolType): string => {
  return PROTOCOL_CONFIG_TIPS[protocolType]?.description || ''
}

const getApiKeyPlaceholder = (protocolType: ProtocolType): string => {
  return PROTOCOL_CONFIG_TIPS[protocolType]?.apiKeyPlaceholder || '请输入API密钥'
}

const getEndpointPlaceholder = (protocolType: ProtocolType): string => {
  return PROTOCOL_CONFIG_TIPS[protocolType]?.endpointPlaceholder || '请输入API端点'
}

const needsExtraConfig = (protocolType: ProtocolType): boolean => {
  return PROTOCOL_CONFIG_TIPS[protocolType]?.extraConfigFields?.length > 0
}

const getExtraConfigTip = (protocolType: ProtocolType): string => {
  const fields = PROTOCOL_CONFIG_TIPS[protocolType]?.extraConfigFields || []
  return `需要配置: ${fields.join(', ')}`
}

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

// Provider change handler
const onProviderChange = () => {
  const provider = modelConfigStore.providers.find(p => p.providerCode === form.value.providerCode)
  if (provider) {
    form.value.apiEndpoint = provider.defaultEndpoint
    form.value.protocolType = provider.providerCode as ProtocolType
    if (!form.value.configName) {
      form.value.configName = `我的${provider.providerName}配置`
    }
  }
}

// Modal handlers
const openAddModal = () => {
  isEditMode.value = false
  editingConfigId.value = null
  form.value = {
    configName: '',
    providerCode: '',
    protocolType: 'OPENAI',
    apiKey: '',
    apiEndpoint: '',
    extraConfig: '',
    isDefault: 0,
    timeoutSeconds: 30,
    maxRetries: 3,
    remark: '',
  }
  showModal.value = true
}

const openEditModal = (config: AiProviderConfigVO) => {
  isEditMode.value = true
  editingConfigId.value = config.id
  form.value = {
    id: config.id,
    configName: config.configName,
    providerCode: config.providerCode,
    protocolType: config.protocolType,
    apiKey: '', // Don't show masked key, user needs to re-enter if changing
    apiEndpoint: config.apiEndpoint,
    extraConfig: config.extraConfig || '',
    isDefault: config.isDefault,
    timeoutSeconds: config.timeoutSeconds || 30,
    maxRetries: config.maxRetries || 3,
    remark: config.remark || '',
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  isEditMode.value = false
  editingConfigId.value = null
}

// Submit handler
const handleSubmit = async () => {
  if (isSubmitting.value) return

  // Validation
  if (!form.value.providerCode || !form.value.configName || !form.value.protocolType) {
    toast.showWarning('请填写所有必填字段')
    return
  }

  // For new config, API key is required
  if (!isEditMode.value && !form.value.apiKey) {
    toast.showWarning('请输入API密钥')
    return
  }

  isSubmitting.value = true

  try {
    if (isEditMode.value && editingConfigId.value) {
      // Update existing config
      const updateData: AiProviderConfigUpdateDTO = {
        id: editingConfigId.value,
        configName: form.value.configName,
        apiEndpoint: form.value.apiEndpoint,
        extraConfig: form.value.extraConfig,
        isDefault: form.value.isDefault,
        timeoutSeconds: form.value.timeoutSeconds,
        maxRetries: form.value.maxRetries,
        remark: form.value.remark,
      }
      // Only include apiKey if user entered a new one
      if (form.value.apiKey) {
        updateData.apiKey = form.value.apiKey
      }
      const success = await providerConfigStore.updateConfig(updateData)
      if (success) {
        closeModal()
      } else {
        toast.showError('更新失败，请稍后重试')
      }
    } else {
      // Add new config
      const success = await providerConfigStore.addConfig(form.value)
      if (success) {
        closeModal()
      } else {
        toast.showError('添加失败，请稍后重试')
      }
    }
  } catch (error) {
    console.error('提交失败:', error)
    toast.showError('操作失败，请检查网络连接')
  } finally {
    isSubmitting.value = false
  }
}

// Delete handler
const handleDelete = async (configId: string) => {
  if (!confirm('确定要删除此供应商配置吗？删除后关联的模型配置可能无法正常使用。')) {
    return
  }

  try {
    const success = await providerConfigStore.deleteConfig(configId)
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
    const success = await providerConfigStore.setDefault(configId)
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
    const result = await providerConfigStore.testConnection(configId)
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
  await modelConfigStore.fetchProviders()
  await providerConfigStore.init()
})
</script>

<style scoped>
.provider-config-management {
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

.provider-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.protocol-badge {
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
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  font-size: var(--font-size-sm);
  color: var(--color-text);
}

.info-value.masked {
  color: var(--color-text-secondary);
}

.info-value.status.active {
  color: var(--color-success);
}

.info-value.status.inactive {
  color: var(--color-danger);
}

/* Card Footer */
.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

/* Form Styles */
.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-row {
  display: flex;
  gap: var(--spacing-lg);
}

.form-group.half {
  flex: 1;
}

.form-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  color: var(--color-text);
  background-color: var(--color-background);
  transition: border-color var(--transition-fast);
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: var(--color-text-tertiary);
}

.form-input:disabled,
.form-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-xs);
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: var(--color-primary);
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

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-text);
}

.btn-secondary:hover {
  background-color: var(--color-background-soft);
}

.btn-sm {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-xs);
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