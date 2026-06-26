<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="['toast', `toast-${toast.type}`]"
      >
        <div class="toast-icon">
          <!-- Success Icon -->
          <svg v-if="toast.type === 'success'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          <!-- Error Icon -->
          <svg v-if="toast.type === 'error'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
          <!-- Warning Icon -->
          <svg v-if="toast.type === 'warning'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <!-- Info Icon -->
          <svg v-if="toast.type === 'info'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
          </svg>
        </div>
        <span class="toast-message">{{ toast.message }}</span>
        <button class="toast-close" @click="removeToast(toast.id)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

const toasts = ref<Toast[]>([])
let toastId = 0

const addToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info', duration: number = 3000) => {
  const id = toastId++
  const toast: Toast = { id, message, type, duration }

  toasts.value.push(toast)

  if (duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }

  return id
}

const removeToast = (id: number) => {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

// 暴露方法给外部使用
defineExpose({
  addToast,
  removeToast,
  success: (message: string, duration?: number) => addToast(message, 'success', duration),
  error: (message: string, duration?: number) => addToast(message, 'error', duration),
  warning: (message: string, duration?: number) => addToast(message, 'warning', duration),
  info: (message: string, duration?: number) => addToast(message, 'info', duration)
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease;
  min-width: 300px;
}

.toast-success {
  border-left: 4px solid #52c41a;
  background: #f6ffed;
}

.toast-error {
  border-left: 4px solid #ff4d4f;
  background: #fff2f0;
}

.toast-warning {
  border-left: 4px solid #faad14;
  background: #fffbe6;
}

.toast-info {
  border-left: 4px solid #1890ff;
  background: #e6f7ff;
}

.toast-icon {
  flex-shrink: 0;
}

.toast-success .toast-icon {
  color: #52c41a;
}

.toast-error .toast-icon {
  color: #ff4d4f;
}

.toast-warning .toast-icon {
  color: #faad14;
}

.toast-info .toast-icon {
  color: #1890ff;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.toast-close {
  flex-shrink: 0;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #999;
  transition: color 0.2s;
}

.toast-close:hover {
  color: #333;
}

/* 动画 */
.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: slideOut 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}
</style>