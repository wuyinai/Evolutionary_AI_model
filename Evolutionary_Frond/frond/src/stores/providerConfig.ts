// AI供应商配置状态管理

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getProviderConfigList,
  addProviderConfig,
  updateProviderConfig,
  deleteProviderConfig,
  setDefaultProviderConfig,
  testProviderConnection,
} from '@/utils/providerConfig'
import type { AiProviderConfigVO, AiProviderConfigAddDTO, AiProviderConfigUpdateDTO } from '@/types/providerConfig'

export const useProviderConfigStore = defineStore('providerConfig', () => {
  // 状态
  const providerConfigs = ref<AiProviderConfigVO[]>([])
  const isLoading = ref(false)
  const currentConfigId = ref<string | null>(null)

  // 计算属性：默认供应商配置
  const defaultConfig = computed(() => {
    return providerConfigs.value.find((config) => config.isDefault === 1)
  })

  // 计算属性：当前选中的供应商配置
  const currentConfig = computed(() => {
    if (currentConfigId.value) {
      return providerConfigs.value.find((config) => config.id === currentConfigId.value)
    }
    return defaultConfig.value
  })

  // 计算属性：是否有供应商配置
  const hasProviderConfig = computed(() => {
    return providerConfigs.value.length > 0
  })

  /**
   * 获取供应商配置列表
   */
  const fetchProviderConfigs = async () => {
    isLoading.value = true
    try {
      const response = await getProviderConfigList()
      if (response.code === 200) {
        providerConfigs.value = response.data
      }
    } catch (error) {
      console.error('获取供应商配置列表失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 添加供应商配置
   */
  const addConfig = async (form: AiProviderConfigAddDTO): Promise<boolean> => {
    isLoading.value = true
    try {
      const response = await addProviderConfig(form)
      if (response.code === 200) {
        // 重新获取列表
        await fetchProviderConfigs()
        return true
      }
      return false
    } catch (error) {
      console.error('添加供应商配置失败:', error)
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 更新供应商配置
   */
  const updateConfig = async (form: AiProviderConfigUpdateDTO): Promise<boolean> => {
    isLoading.value = true
    try {
      const response = await updateProviderConfig(form)
      if (response.code === 200) {
        // 重新获取列表
        await fetchProviderConfigs()
        return true
      }
      return false
    } catch (error) {
      console.error('更新供应商配置失败:', error)
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 删除供应商配置
   */
  const deleteConfig = async (configId: string): Promise<boolean> => {
    try {
      const response = await deleteProviderConfig(configId)
      if (response.code === 200) {
        // 从列表中移除
        providerConfigs.value = providerConfigs.value.filter((config) => config.id !== configId)
        // 如果删除的是当前选中的配置，清除选中状态
        if (currentConfigId.value === configId) {
          currentConfigId.value = null
        }
        return true
      }
      return false
    } catch (error) {
      console.error('删除供应商配置失败:', error)
      return false
    }
  }

  /**
   * 设置默认供应商配置
   */
  const setDefault = async (configId: string): Promise<boolean> => {
    try {
      const response = await setDefaultProviderConfig(configId)
      if (response.code === 200) {
        // 更新本地状态
        providerConfigs.value.forEach((config) => {
          config.isDefault = config.id === configId ? 1 : 0
        })
        return true
      }
      return false
    } catch (error) {
      console.error('设置默认供应商配置失败:', error)
      return false
    }
  }

  /**
   * 测试供应商连接
   */
  const testConnection = async (configId: string): Promise<string> => {
    try {
      const response = await testProviderConnection(configId)
      if (response.code === 200) {
        return response.data
      }
      return '测试失败'
    } catch (error) {
      console.error('测试供应商连接失败:', error)
      return '连接失败'
    }
  }

  /**
   * 选择供应商配置
   */
  const selectConfig = (configId: string | null) => {
    currentConfigId.value = configId
  }

  /**
   * 初始化
   */
  const init = async () => {
    await fetchProviderConfigs()
  }

  return {
    // 状态
    providerConfigs,
    isLoading,
    currentConfigId,
    // 计算属性
    defaultConfig,
    currentConfig,
    hasProviderConfig,
    // 方法
    fetchProviderConfigs,
    addConfig,
    updateConfig,
    deleteConfig,
    setDefault,
    testConnection,
    selectConfig,
    init,
  }
})