<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-container">
      <div class="modal-header">
        <h3 class="modal-title">添加模型配置</h3>
        <button class="close-btn" @click="close">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <div class="modal-body">
        <!-- Architecture Info Banner -->
        <div class="architecture-banner">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
          </svg>
          <span>新架构：模型配置关联供应商配置，连接信息由供应商配置管理</span>
        </div>

        <form @submit.prevent="handleSubmit">
          <!-- 选择供应商配置（新架构） -->
          <div class="form-group">
            <label class="form-label">供应商配置</label>
            <select v-model="form.providerConfigId" class="form-select" required @change="onProviderConfigChange">
              <option value="">请选择供应商配置</option>
              <option v-for="config in providerConfigStore.providerConfigs" :key="config.id" :value="config.id">
                {{ config.configName }} ({{ config.providerName }} - {{ config.protocolType }})
              </option>
            </select>
            <p v-if="!providerConfigStore.hasProviderConfig" class="form-tip warning">
              暂无供应商配置，请先<a href="#" @click.prevent="goToProviderConfig">创建供应商配置</a>
            </p>
          </div>

          <!-- 配置名称 -->
          <div class="form-group">
            <label class="form-label">配置名称</label>
            <input
              v-model="form.configName"
              type="text"
              class="form-input"
              placeholder="例如：我的DeepSeek聊天模型"
              required
            />
          </div>

          <!-- 模型名称 -->
          <div class="form-group">
            <label class="form-label">模型名称</label>
            <input
              v-model="form.modelName"
              type="text"
              class="form-input"
              placeholder="例如：deepseek-chat"
              required
            />
            <p class="form-tip">请输入供应商支持的模型名称</p>
          </div>

          <!-- 模型类型 -->
          <div class="form-group">
            <label class="form-label">模型类型</label>
            <select v-model="form.modelType" class="form-select" required>
              <option value="CHAT">对话模型（CHAT）</option>
              <option value="EMBEDDING">向量模型（EMBEDDING）</option>
            </select>
            <p class="form-tip">选择模型的用途类型</p>
          </div>

          <!-- 向量模型特有配置 -->
          <div v-if="form.modelType === 'EMBEDDING'" class="embedding-config">
            <!-- 向量维度 -->
            <div class="form-group">
              <label class="form-label">向量维度</label>
              <input
                v-model.number="form.vectorDimensions"
                type="number"
                class="form-input"
                placeholder="例如：1536"
                min="1"
              />
              <p class="form-tip">向量模型的输出维度，常见值：768、1024、1536</p>
            </div>

            <!-- 相似度阈值 -->
            <div class="form-group">
              <label class="form-label">相似度阈值</label>
              <div class="temperature-slider">
                <input
                  v-model.number="form.similarityThreshold"
                  type="range"
                  min="0"
                  max="1"
                  step="0.01"
                  class="slider"
                />
                <span class="temperature-value">{{ form.similarityThreshold }}</span>
              </div>
              <p class="form-tip">向量相似度的最小阈值，低于此值的结果将被过滤</p>
            </div>
          </div>

          <!-- 对话模型特有配置 -->
          <div v-if="form.modelType === 'CHAT'" class="chat-config">
            <!-- 模型别名（可选） -->
            <div class="form-group">
              <label class="form-label">模型别名（可选）</label>
              <input
                v-model="form.modelAlias"
                type="text"
                class="form-input"
                placeholder="例如：DeepSeek聊天助手"
              />
              <p class="form-tip">自定义显示名称，便于识别</p>
            </div>

            <!-- 温度参数 -->
            <div class="form-group">
              <label class="form-label">温度参数</label>
              <div class="temperature-slider">
                <input
                  v-model.number="form.temperature"
                  type="range"
                  min="0"
                  max="2"
                  step="0.1"
                  class="slider"
                />
                <span class="temperature-value">{{ form.temperature }}</span>
              </div>
              <p class="form-tip">值越高回复越随机，值越低回复越确定</p>
            </div>

            <!-- 最大Token数 -->
            <div class="form-group">
              <label class="form-label">最大输出Token数</label>
              <input
                v-model.number="form.maxTokens"
                type="number"
                class="form-input"
                placeholder="例如：4096"
                min="1"
                max="128000"
              />
              <p class="form-tip">限制模型输出的最大Token数量</p>
            </div>

            <!-- Top-P参数 -->
            <div class="form-group">
              <label class="form-label">Top-P采样参数</label>
              <div class="temperature-slider">
                <input
                  v-model.number="form.topP"
                  type="range"
                  min="0"
                  max="1"
                  step="0.1"
                  class="slider"
                />
                <span class="temperature-value">{{ form.topP }}</span>
              </div>
              <p class="form-tip">控制采样的多样性，建议与温度参数调整其中一个</p>
            </div>

            <!-- 是否启用流式输出 -->
            <div class="form-group checkbox-group">
              <label class="checkbox-label">
                <input v-model="form.isStreamingEnabled" type="checkbox" :true-value="1" :false-value="0" />
                <span>启用流式输出</span>
              </label>
              <p class="form-tip">实时返回AI回复内容</p>
            </div>
          </div>

          <!-- 模型别名（可选）- 向量模型也显示 -->
          <div v-if="form.modelType === 'EMBEDDING'" class="form-group">
            <label class="form-label">模型别名（可选）</label>
            <input
              v-model="form.modelAlias"
              type="text"
              class="form-input"
              placeholder="例如：OpenAI向量模型"
            />
            <p class="form-tip">自定义显示名称，便于识别</p>
          </div>

          <!-- 设为默认 -->
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input v-model="form.isDefault" type="checkbox" :true-value="1" :false-value="0" />
              <span>设为默认模型</span>
            </label>
          </div>

          <!-- 备注 -->
          <div class="form-group">
            <label class="form-label">备注（可选）</label>
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
        <button class="btn btn-secondary" @click="close">取消</button>
        <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
          <span v-if="isSubmitting">添加中...</span>
          <span v-else>添加</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useModelConfigStore } from '@/stores/modelConfig'
import { useProviderConfigStore } from '@/stores/providerConfig'
import type { AiModelConfigAddDTO } from '@/types/modelConfig'

// Router
const router = useRouter()

// Emits
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

// Stores
const modelConfigStore = useModelConfigStore()
const providerConfigStore = useProviderConfigStore()

// Form data (new architecture)
const form = ref<AiModelConfigAddDTO>({
  configName: '',
  providerConfigId: '',
  modelName: '',
  modelAlias: '',
  modelType: 'CHAT', // 默认为对话模型
  vectorDimensions: 1536, // 默认向量维度
  similarityThreshold: 0.75, // 默认相似度阈值
  temperature: 0.7,
  maxTokens: 4096,
  topP: 1.0,
  isDefault: 0,
  isStreamingEnabled: 1,
  remark: '',
})

const isSubmitting = ref(false)

// Provider config change handler
const onProviderConfigChange = () => {
  const config = providerConfigStore.providerConfigs.find(c => c.id === form.value.providerConfigId)
  if (config) {
    // Auto-fill config name
    if (!form.value.configName) {
      form.value.configName = `我的${config.providerName}模型`
    }
  }
}

// Navigate to provider config page
const goToProviderConfig = () => {
  close()
  router.push('/provider-config')
}

// Close modal
const close = () => {
  emit('close')
}

// Submit handler
const handleSubmit = async () => {
  if (isSubmitting.value) return

  // Validation
  if (!form.value.providerConfigId || !form.value.configName || !form.value.modelName) {
    alert('请填写所有必填字段')
    return
  }

  isSubmitting.value = true

  try {
    const success = await modelConfigStore.addConfig(form.value)
    if (success) {
      emit('success')
      close()
    } else {
      alert('添加失败，请稍后重试')
    }
  } catch (error) {
    console.error('添加模型配置失败:', error)
    alert('添加失败，请检查网络连接')
  } finally {
    isSubmitting.value = false
  }
}

// Initialize
onMounted(async () => {
  await providerConfigStore.init()
})
</script>

<style scoped>
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
  width: 520px;
  max-width: 90vw;
  max-height: 90vh;
  border-radius: var(--radius-lg);
  background-color: var(--color-background);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
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

/* Architecture Banner */
.architecture-banner {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background-color: var(--color-primary-light);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
  font-size: var(--font-size-sm);
  color: var(--color-primary);
}

.architecture-banner svg {
  flex-shrink: 0;
}

.form-group {
  margin-bottom: var(--spacing-lg);
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

.form-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-xs);
}

.form-tip.warning {
  color: var(--color-warning);
}

.form-tip.warning a {
  color: var(--color-primary);
  text-decoration: underline;
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
}

.temperature-slider {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.slider {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--color-border);
  appearance: none;
}

.slider::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary);
  cursor: pointer;
}

.temperature-value {
  min-width: 40px;
  font-size: var(--font-size-sm);
  color: var(--color-text);
  text-align: center;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.btn {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-primary {
  border: none;
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
</style>