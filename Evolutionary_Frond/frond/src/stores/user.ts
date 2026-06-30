// 用户状态管理

import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { post, get } from '@/utils/request'
import { setToken, removeToken, setUserInfo, removeUserInfo, getToken, getUserInfo as getStoredUserInfo } from '@/utils/auth'
import type { User, LoginForm, RegisterForm, LoginResponse, RegisterResponse, UserResponse } from '@/types/user'
import type { SysPermission } from '@/utils/sysPermissionApi'

const USER_STORE_KEY = 'user-store'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(getToken())
  const userInfo = ref<User | null>(getStoredUserInfo())
  const isLoggedIn = ref<boolean>(!!token.value)
  const userMenus = ref<SysPermission[]>([])  // 当前用户的菜单树

  /**
   * 从 localStorage 恢复状态
   */
  const restoreState = () => {
    const savedToken = getToken()
    const savedUserInfo = getStoredUserInfo()
    
    if (savedToken) {
      token.value = savedToken
      isLoggedIn.value = true
    }
    
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }
  }

  /**
   * 持久化状态到 localStorage
   */
  const persistState = () => {
    if (token.value) {
      setToken(token.value)
    } else {
      removeToken()
    }
    
    if (userInfo.value) {
      setUserInfo(userInfo.value)
    } else {
      removeUserInfo()
    }
  }

  /**
   * 重置状态到初始值
   */
  const reset = () => {
    token.value = null
    userInfo.value = null
    isLoggedIn.value = false
    userMenus.value = []
    removeToken()
    removeUserInfo()
  }

  // 初始化时恢复状态
  restoreState()

  // 监听状态变化并自动持久化
  watch([token, userInfo, isLoggedIn], () => {
    persistState()
  }, { deep: true })

  /**
   * 用户登录
   */
  const login = async (loginForm: LoginForm): Promise<LoginResponse> => {
    try {
      const response = await post<LoginResponse>('/auth/login', {
        username: loginForm.username,
        password: loginForm.password,
      })

      if (response.code === 200 && response.data.token) {
        token.value = response.data.token
        isLoggedIn.value = true
        setToken(response.data.token)
      }

      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * 用户注册
   */
  const register = async (registerForm: RegisterForm): Promise<RegisterResponse> => {
    try {
      const response = await post<RegisterResponse>('/auth/register', {
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password,
        confirmPassword: registerForm.confirmPassword,
      })

      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * 获取用户信息
   */
  const getUserInfo = async (userId: number): Promise<UserResponse> => {
    try {
      const response = await get<UserResponse>(`/system/user/${userId}`)

      if (response.code === 200 && response.data) {
        userInfo.value = response.data
        setUserInfo(response.data)
      }

      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * 用户登出
   */
  const logout = () => {
    reset()
  }

  /**
   * 初始化用户状态（已通过 restoreState 实现）
   */
  const initUserState = () => {
    restoreState()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userMenus,
    login,
    register,
    getUserInfo,
    logout,
    reset,
    initUserState,
  }
})
