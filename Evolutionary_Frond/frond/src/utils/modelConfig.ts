// AI模型配置API服务

import { get, post, put, del } from '@/utils/request'
import type {
  ModelConfigListResponse,
  ProviderListResponse,
  AddModelConfigResponse,
  TestConnectionResponse,
  AiModelConfigAddForm,
} from '@/types/modelConfig'

/**
 * 获取用户的模型配置列表
 */
export const getModelConfigList = (): Promise<ModelConfigListResponse> => {
  return get('/ai/config/list')
}

/**
 * 获取供应商列表
 */
export const getProviderList = (): Promise<ProviderListResponse> => {
  return get('/ai/provider/list')
}

/**
 * 添加模型配置
 */
export const addModelConfig = (data: AiModelConfigAddForm): Promise<AddModelConfigResponse> => {
  return post('/ai/config/add', data)
}

/**
 * 更新模型配置
 */
export const updateModelConfig = (id: string, data: Partial<AiModelConfigAddForm>) => { // 使用string类型
  return put('/ai/config/update', { id, ...data })
}

/**
 * 删除模型配置
 */
export const deleteModelConfig = (id: string) => { // 使用string类型
  return del(`/ai/config/delete/${id}`)
}

/**
 * 设置默认模型
 */
export const setDefaultModel = (id: string) => { // 使用string类型
  return put(`/ai/config/set-default/${id}`)
}

/**
 * 测试模型连接
 */
export const testModelConnection = (id: string): Promise<TestConnectionResponse> => { // 使用string类型
  return post(`/ai/config/test/${id}`)
}