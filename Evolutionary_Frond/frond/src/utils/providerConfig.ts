// AI供应商配置API服务

import { get, post, put, del } from '@/utils/request'
import type {
  ProviderConfigListResponse,
  AddProviderConfigResponse,
  TestProviderConnectionResponse,
  AiProviderConfigAddDTO,
  AiProviderConfigUpdateDTO,
} from '@/types/providerConfig'

/**
 * 获取用户的供应商配置列表
 */
export const getProviderConfigList = (): Promise<ProviderConfigListResponse> => {
  return get('/ai/provider-config/list')
}

/**
 * 添加供应商配置
 */
export const addProviderConfig = (data: AiProviderConfigAddDTO): Promise<AddProviderConfigResponse> => {
  return post('/ai/provider-config/add', data)
}

/**
 * 更新供应商配置
 */
export const updateProviderConfig = (data: AiProviderConfigUpdateDTO) => {
  return put('/ai/provider-config/update', data)
}

/**
 * 删除供应商配置
 */
export const deleteProviderConfig = (id: string) => {
  return del(`/ai/provider-config/delete/${id}`)
}

/**
 * 设置默认供应商配置
 */
export const setDefaultProviderConfig = (id: string) => {
  return put(`/ai/provider-config/set-default/${id}`)
}

/**
 * 测试供应商连接
 */
export const testProviderConnection = (id: string): Promise<TestProviderConnectionResponse> => {
  return post(`/ai/provider-config/test/${id}`)
}