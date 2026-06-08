<template>
  <div class="model-selector">
    <!-- 模型选择按钮 -->
    <button
      class="selector-btn"
      @click="toggleDropdown"
      :disabled="disabled"
    >
      <!-- 有默认模型时显示模型名称 -->
      <template v-if="modelConfigStore.currentModel">
        <svg class="model-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
          <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
          <line x1="12" y1="22.08" x2="12" y2="12"></line>
        </svg>
        <span class="model-name">{{ displayModelName }}</span>
      </template>
      <!-- 无模型时显示加号 -->
      <template v-else>
        <svg class="add-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span class="add-text">添加模型</span>
      </template>
    </button>

    <!-- 下拉菜单 -->
    <div v-if="showDropdown" class="dropdown-menu">
      <!-- 已有模型列表 -->
      <div v-if="modelConfigStore.hasModelConfig" class="model-list">
        <div
          v-for="config in modelConfigStore.modelConfigs"
          :key="config.id"
          class="model-item"
          :class="{ selected: modelConfigStore.currentConfigId === config.id }"
          @click="selectModel(config)"
        >
          <div class="model-info">
            <span class="model-name-text">{{ config.configName }}</span>
            <span class="model-provider">{{ config.providerName }}</span>
          </div>
          <div class="model-actions">
            <span v-if="config.isDefault === 1" class="default-badge">默认</span>
            <button class="set-default-btn" @click.stop="setDefault(config.id)" title="设为默认">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 无模型时的提示 -->
      <div v-else class="no-model-tip">
        <p>暂无模型配置</p>
        <p class="tip-text">点击下方按钮添加新模型</p>
      </div>

      <!-- 添加新模型按钮 -->
      <button class="add-model-btn" @click="openAddModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>添加新模型</span>
      </button>
    </div>

    <!-- 添加模型弹窗 -->
    <AddModelModal
      v-if="showAddModal"
      @close="closeAddModal"
      @success="onAddSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useModelConfigStore } from '@/stores/modelConfig'
import AddModelModal from './AddModelModal.vue'
import type { AiModelConfigVO } from '@/types/modelConfig'

// Props
const props = defineProps<{
  disabled?: boolean
}>()

// Emits
const emit = defineEmits<{
  (e: 'change', configId: string | null): void // 使用string类型
}>()

const modelConfigStore = useModelConfigStore()

const showDropdown = ref(false)
const showAddModal = ref(false)

// 显示的模型名称
const displayModelName = computed(() => {
  const model = modelConfigStore.currentModel
  if (model) {
    return model.modelAlias || model.configName
  }
  return ''
})

// 切换下拉菜单
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// 选择模型
const selectModel = (config: AiModelConfigVO) => {
  modelConfigStore.selectModel(config.id)
  emit('change', config.id)
  showDropdown.value = false
}

// 设置默认模型
const setDefault = async (configId: string) => { // 使用string类型
  await modelConfigStore.setDefault(configId)
}

// 打开添加弹窗
const openAddModal = () => {
  showDropdown.value = false
  showAddModal.value = true
}

// 关闭添加弹窗
const closeAddModal = () => {
  showAddModal.value = false
}

// 添加成功回调
const onAddSuccess = () => {
  closeAddModal()
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.model-selector')) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  // 初始化加载模型配置
  modelConfigStore.init()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.model-selector {
  position: relative;
}

.selector-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background-color: var(--color-background);
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.selector-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  background-color: var(--color-background-soft);
}

.selector-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.model-icon,
.add-icon {
  color: var(--color-primary);
}

.model-name,
.add-text {
  font-size: var(--font-size-sm);
}

.dropdown-menu {
  position: absolute;
  bottom: calc(100% + var(--spacing-sm));
  left: 0;
  min-width: 280px;
  max-width: 320px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background-color: var(--color-background);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  z-index: 100;
}

.model-list {
  max-height: 300px;
  overflow-y: auto;
}

.model-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.model-item:hover {
  background-color: var(--color-background-soft);
}

.model-item.selected {
  background-color: var(--color-primary-light);
}

.model-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.model-name-text {
  font-size: var(--font-size-base);
  color: var(--color-text);
  font-weight: 500;
}

.model-provider {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.model-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.default-badge {
  font-size: var(--font-size-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  background-color: var(--color-primary);
  color: white;
}

.set-default-btn {
  padding: var(--spacing-xs);
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.set-default-btn:hover {
  color: var(--color-primary);
}

.no-model-tip {
  padding: var(--spacing-lg);
  text-align: center;
}

.no-model-tip p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.tip-text {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.add-model-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  width: 100%;
  padding: var(--spacing-md);
  border: none;
  border-top: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.add-model-btn:hover {
  background-color: var(--color-primary-light);
}
</style>