// 认证相关工具函数

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

/**
 * 获取存储的 Token
 */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否已登录
 */
export const isAuthenticated = (): boolean => {
  return !!getToken()
}

/**
 * 获取存储的用户信息
 */
export const getUserInfo = (): any => {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 */
export const setUserInfo = (userInfo: any): void => {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export const removeUserInfo = (): void => {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 清除所有认证信息
 */
export const clearAuth = (): void => {
  removeToken()
  removeUserInfo()
}

/**
 * 解析 JWT Token
 */
export const parseJwt = (token: string): any => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        })
        .join(''),
    )
    return JSON.parse(jsonPayload)
  } catch (e) {
    return null
  }
}

/**
 * 检查 Token 是否过期
 */
export const isTokenExpired = (token: string): boolean => {
  const decoded = parseJwt(token)
  if (!decoded || !decoded.exp) {
    return true
  }
  // exp 是秒级时间戳，需要乘以 1000 转换为毫秒
  return decoded.exp * 1000 < Date.now()
}
