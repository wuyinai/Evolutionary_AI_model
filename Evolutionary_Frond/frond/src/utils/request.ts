// Axios HTTP 客户端配置

import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, AxiosError } from 'axios'
import type { ApiResponse } from '@/types/api'
import { AppError, AppErrorType } from '@/types/api'
import { useToast } from '@/composables/useToast'
import { clearAuth } from '@/utils/auth'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8234',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 直接返回响应数据
    return response.data as any
  },
  (error: AxiosError) => {
    const toast = useToast()
    let appError: AppError

    // 处理错误响应
    if (error.response) {
      const { status, data } = error.response
      const message = (data as any)?.message

      // 根据状态码创建对应的AppError
      appError = AppError.fromHttpStatus(status, message)

      // 显示用户友好的错误提示
      switch (status) {
        case 401:
          // Token 过期或无效，清除本地存储并跳转到登录页
          clearAuth()
          window.location.href = '/login'
          break
        case 403:
          toast.showError('权限不足，无法访问该资源')
          break
        case 404:
          toast.showError('请求的资源不存在')
          break
        default:
          if (status >= 500) {
            toast.showError('服务器错误，请稍后重试')
          } else {
            toast.showError(message || '请求失败')
          }
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      if (error.code === 'ECONNABORTED') {
        appError = AppError.timeoutError()
        toast.showError('请求超时，请稍后重试')
      } else {
        appError = AppError.networkError()
        toast.showError('网络错误，请检查您的网络连接')
      }
    } else {
      // 请求配置出错
      appError = AppError.unknownError(error)
      toast.showError('请求配置错误')
    }

    // 将原始错误附加到AppError上，方便调试
    appError.originalError = error

    return Promise.reject(appError)
  },
)

// 封装 GET 请求
export const get = <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.get(url, config)
}

// 封装 POST 请求
export const post = <T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig,
): Promise<ApiResponse<T>> => {
  return request.post(url, data, config)
}

// 封装 PUT 请求
export const put = <T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig,
): Promise<ApiResponse<T>> => {
  return request.put(url, data, config)
}

// 封装 DELETE 请求
export const del = <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.delete(url, config)
}

export default request
