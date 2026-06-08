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
        <form @submit.prevent="handleSubmit">
          <!-- 选择供应商 -->
          <div class="form-group">
            <label class="form-label">供应商</label>
            <select v-model="form.providerCode" class="form-select" required @change="onProviderChange">
              <option value="">请选择供应商</option>
              <option v-for="provider in modelConfigStore.providers" :key="provider.providerCode" :value="provider.providerCode">
                {{ provider.providerName }}
              </option>
            </select>
          </div>

          <!-- 配置名称 -->
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

          <!-- API密钥 -->
          <div class="form-group">
            <label class="form-label">API密钥</label>
            <input
              v-model="form.apiKey"
              type="password"
              class="form-input"
              placeholder="sk-xxxxxxxxxxxxx"
              required
            />
          </div>

          <!-- API端点（可选） -->
          <div class="form-group">
            <label class="form-label">API端点（可选）</label>
            <input
              v-model="form.apiEndpoint"
              type="text"
              class="form-input"
              :placeholder="defaultEndpointPlaceholder"
            />
            <p class="form-tip">留空将使用供应商默认端点</p>
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

          <!-- 设为默认 -->
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input v-model="form.isDefault" type="checkbox" :true-value="1" :false-value="0" />
              <span>设为默认模型</span>
            </label>
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
import { ref, computed, watch } from 'vue'
import { useModelConfigStore } from '@/stores/modelConfig'
import type { AiModelConfigAddForm } from '@/types/modelConfig'

// Emits
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const modelConfigStore = useModelConfigStore()

const form = ref<AiModelConfigAddForm>({
  configName: '',
  providerCode: '',
  modelName: '',
  apiKey: '',
  apiEndpoint: '',
  temperature: 0.7,
  isDefault: 0,
  isStreamingEnabled: 1,
})

const isSubmitting = ref(false)

// 默认端点提示
const defaultEndpointPlaceholder = computed(() => {
  const provider = modelConfigStore.providers.find(p => p.providerCode === form.value.providerCode)
  return provider ? provider.defaultEndpoint : '请先选择供应商'
})

// 供应商变更时自动填充默认端点
const onProviderChange = () => {
  const provider = modelConfigStore.providers.find(p => p.providerCode === form.value.providerCode)
  if (provider) {
    form.value.apiEndpoint = provider.defaultEndpoint
    // 自动填充配置名称
    if (!form.value.configName) {
      form.value.configName = `我的${provider.providerName}配置`
    }
  }
}

// 关闭弹窗
const close = () => {
  emit('close')
}

// 提交表单
const handleSubmit = async () => {
  if (isSubmitting.value) return

  // 验证必填字段
  if (!form.value.providerCode || !form.value.configName || !form.value.modelName || !form.value.apiKey) {
    alert('请填写所有必填字段')
    return
  }

  isSubmitting.value = true

  try {
    const success = await modelConfigStore.addConfig(form.value)
    if (success) {
      emit('success')
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
  width: 480px;
  max-width: 90vw;
  border-radius: var(--radius-lg);
  background-color: var(--color-background);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
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
  max-height: 60vh;
  overflow-y: auto;
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
.form-select {
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
.form-select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-input::placeholder {
  color: var(--color-text-tertiary);
}

.form-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-xs);
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