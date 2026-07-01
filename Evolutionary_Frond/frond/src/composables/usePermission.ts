// 权限校验 composable

import { ref, computed } from 'vue'
import { getUserPermissionCodes } from '@/utils/sysPermissionApi'

// 权限码缓存（全局单例）
const permissionCodes = ref<string[]>([])
const loading = ref(false)
const loaded = ref(false)

/**
 * 加载当前用户的权限码列表
 */
const loadPermissions = async (): Promise<void> => {
  if (loaded.value) return
  loading.value = true
  try {
    const response = await getUserPermissionCodes()
    if (response.code === 200 && response.data) {
      permissionCodes.value = response.data
      loaded.value = true
    }
  } catch (error) {
    console.error('加载权限码失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 检查是否拥有指定权限
 * @param code 权限码，如 'sys:user:add'
 * @returns 是否拥有该权限
 */
const hasPermission = (code: string): boolean => {
  if (!loaded.value) {
    // 如果还没加载，触发加载（异步）
    loadPermissions()
  }
  return permissionCodes.value.includes(code)
}

/**
 * 检查是否拥有任意一个指定权限
 * @param codes 权限码数组
 * @returns 是否拥有任意一个权限
 */
const hasAnyPermission = (codes: string[]): boolean => {
  if (!loaded.value) {
    loadPermissions()
  }
  return codes.some(code => permissionCodes.value.includes(code))
}

/**
 * 检查是否拥有所有指定权限
 * @param codes 权限码数组
 * @returns 是否拥有所有权限
 */
const hasAllPermissions = (codes: string[]): boolean => {
  if (!loaded.value) {
    loadPermissions()
  }
  return codes.every(code => permissionCodes.value.includes(code))
}

/**
 * 清除权限缓存（用于登出时）
 */
const clearPermissions = (): void => {
  permissionCodes.value = []
  loaded.value = false
}

/**
 * 权限校验 composable
 */
export function usePermission() {
  return {
    permissionCodes: computed(() => permissionCodes.value),
    loading: computed(() => loading.value),
    loaded: computed(() => loaded.value),
    loadPermissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    clearPermissions
  }
}