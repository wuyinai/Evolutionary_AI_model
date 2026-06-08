// AI模型配置状态管理

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getModelConfigList,
  getProviderList,
  addModelConfig,
  setDefaultModel,
  deleteModelConfig,
  testModelConnection,
} from '@/utils/modelConfig'
import type { AiModelConfigVO, AiModelProviderVO, AiModelConfigAddDTO } from '@/types/modelConfig'

export const useModelConfigStore = defineStore('modelConfig', () => {
  // 状态
  const modelConfigs = ref<AiModelConfigVO[]>([])
  const providers = ref<AiModelProviderVO[]>([])
  const currentConfigId = ref<string | null>(null) // 使用string类型
  const isLoading = ref(false)

  // 计算属性：默认模型
  const defaultModel = computed(() => {
    return modelConfigs.value.find((config) => config.isDefault === 1)
  })

  // 计算属性：当前选中的模型
  const currentModel = computed(() => {
    if (currentConfigId.value) {
      return modelConfigs.value.find((config) => config.id === currentConfigId.value)
    }
    return defaultModel.value
  })

  // 计算属性：是否有模型配置
  const hasModelConfig = computed(() => {
    return modelConfigs.value.length > 0
  })

  /**
   * 获取模型配置列表
   */
  const fetchModelConfigs = async () => {
    isLoading.value = true
    try {
      const response = await getModelConfigList()
      if (response.code === 200) {
        modelConfigs.value = response.data
      }
    } catch (error) {
      console.error('获取模型配置列表失败:', error)
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取供应商列表
   */
  const fetchProviders = async () => {
    try {
      const response = await getProviderList()
      if (response.code === 200) {
        providers.value = response.data
      }
    } catch (error) {
      console.error('获取供应商列表失败:', error)
    }
  }

  /**
   * 添加模型配置
   */
  const addConfig = async (form: AiModelConfigAddDTO): Promise<boolean> => {
    isLoading.value = true
    try {
      const response = await addModelConfig(form)
      if (response.code === 200) {
        // 重新获取列表
        await fetchModelConfigs()
        return true
      }
      return false
    } catch (error) {
      console.error('添加模型配置失败:', error)
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 设置默认模型
   */
  const setDefault = async (configId: string): Promise<boolean> => { // 使用string类型
    try {
      const response = await setDefaultModel(configId)
      if (response.code === 200) {
        // 更新本地状态
        modelConfigs.value.forEach((config) => {
          config.isDefault = config.id === configId ? 1 : 0
        })
        return true
      }
      return false
    } catch (error) {
      console.error('设置默认模型失败:', error)
      return false
    }
  }

  /**
   * 删除模型配置
   */
  const deleteConfig = async (configId: string): Promise<boolean> => { // 使用string类型
    try {
      const response = await deleteModelConfig(configId)
      if (response.code === 200) {
        // 从列表中移除
        modelConfigs.value = modelConfigs.value.filter((config) => config.id !== configId)
        // 如果删除的是当前选中的模型，清除选中状态
        if (currentConfigId.value === configId) {
          currentConfigId.value = null
        }
        return true
      }
      return false
    } catch (error) {
      console.error('删除模型配置失败:', error)
      return false
    }
  }

  /**
   * 测试模型连接
   */
  const testConnection = async (configId: string): Promise<string> => { // 使用string类型
    try {
      const response = await testModelConnection(configId)
      if (response.code === 200) {
        return response.data
      }
      return '测试失败'
    } catch (error) {
      console.error('测试模型连接失败:', error)
      return '连接失败'
    }
  }

  /**
   * 选择模型
   */
  const selectModel = (configId: string | null) => { // 使用string类型
    currentConfigId.value = configId
  }

  /**
   * 初始化
   */
  const init = async () => {
    await fetchProviders()
    await fetchModelConfigs()
  }

  return {
    // 状态
    modelConfigs,
    providers,
    currentConfigId,
    isLoading,
    // 计算属性
    defaultModel,
    currentModel,
    hasModelConfig,
    // 方法
    fetchModelConfigs,
    fetchProviders,
    addConfig,
    setDefault,
    deleteConfig,
    testConnection,
    selectModel,
    init,
  }
})