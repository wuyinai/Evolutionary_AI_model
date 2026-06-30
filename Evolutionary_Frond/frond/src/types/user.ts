// 用户相关类型定义

export interface User {
  userId: number
  username: string
  email: string
  realName?: string
  phone?: string
  avatar?: string
  gender?: number
  status?: number
  createTime: string
  updateTime: string
  lastLoginTime?: string
}

export interface LoginForm {
  username: string
  password: string
  rememberMe?: boolean
}

export interface RegisterForm {
  username: string
  email: string
  password: string
  confirmPassword: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  code: number
  message: string
  data: {
    token: string
  }
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  code?: string
}

export interface RegisterResponse {
  code: number
  message: string
  data: null
}

export interface UserResponse {
  code: number
  message: string
  data: User
}
