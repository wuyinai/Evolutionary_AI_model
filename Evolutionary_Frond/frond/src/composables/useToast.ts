// Toast composable - 提供全局toast服务

import { ref, type Ref } from 'vue'

interface ToastMethods {
  success: (message: string, duration?: number) => void
  error: (message: string, duration?: number) => void
  warning: (message: string, duration?: number) => void
  info: (message: string, duration?: number) => void
}

// 全局toast实例引用
const toastInstance: Ref<ToastMethods | null> = ref(null)

/**
 * 设置toast组件实例（在App.vue中调用）
 */
export const setToastInstance = (instance: ToastMethods) => {
  toastInstance.value = instance
}

/**
 * Toast composable
 * 在组件中使用，提供toast服务
 */
export const useToast = () => {
  const showSuccess = (message: string, duration: number = 3000) => {
    if (toastInstance.value) {
      toastInstance.value.success(message, duration)
    } else {
      console.warn('Toast instance not initialized')
      // fallback to alert
      alert(message)
    }
  }

  const showError = (message: string, duration: number = 4000) => {
    if (toastInstance.value) {
      toastInstance.value.error(message, duration)
    } else {
      console.warn('Toast instance not initialized')
      // fallback to alert
      alert(message)
    }
  }

  const showWarning = (message: string, duration: number = 3500) => {
    if (toastInstance.value) {
      toastInstance.value.warning(message, duration)
    } else {
      console.warn('Toast instance not initialized')
      alert(message)
    }
  }

  const showInfo = (message: string, duration: number = 3000) => {
    if (toastInstance.value) {
      toastInstance.value.info(message, duration)
    } else {
      console.warn('Toast instance not initialized')
      alert(message)
    }
  }

  return {
    showSuccess,
    showError,
    showWarning,
    showInfo
  }
}