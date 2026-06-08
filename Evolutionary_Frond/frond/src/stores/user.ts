// 用户状态管理

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/utils/request'
import { setToken, removeToken, setUserInfo, removeUserInfo, getToken } from '@/utils/auth'
import type { User, LoginForm, RegisterForm, LoginResponse, RegisterResponse, UserResponse } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(getToken())
  const userInfo = ref<User | null>(null)
  const isLoggedIn = ref<boolean>(!!token.value)

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
    token.value = null
    userInfo.value = null
    isLoggedIn.value = false
    removeToken()
    removeUserInfo()
  }

  /**
   * 初始化用户状态
   */
  const initUserState = () => {
    const savedToken = getToken()
    if (savedToken) {
      token.value = savedToken
      isLoggedIn.value = true
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    getUserInfo,
    logout,
    initUserState,
  }
})
