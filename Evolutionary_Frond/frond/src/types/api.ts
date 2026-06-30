// API 通用响应类型

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface ApiError {
  code: number
  message: string
  details?: string
}

/**
 * 应用错误类型枚举
 */
export enum AppErrorType {
  NETWORK_ERROR = 'NETWORK_ERROR',
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  NOT_FOUND = 'NOT_FOUND',
  SERVER_ERROR = 'SERVER_ERROR',
  TIMEOUT = 'TIMEOUT',
  BUSINESS_ERROR = 'BUSINESS_ERROR',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
}

/**
 * 统一应用错误类
 */
export class AppError extends Error {
  type: AppErrorType
  code?: number
  details?: string
  originalError?: unknown

  constructor(
    type: AppErrorType,
    message: string,
    options?: {
      code?: number
      details?: string
      originalError?: unknown
    },
  ) {
    super(message)
    this.name = 'AppError'
    this.type = type
    this.code = options?.code
    this.details = options?.details
    this.originalError = options?.originalError
  }

  /**
   * 根据HTTP状态码创建对应的AppError
   */
  static fromHttpStatus(status: number, message?: string): AppError {
    switch (status) {
      case 401:
        return new AppError(AppErrorType.UNAUTHORIZED, message || '未授权，请重新登录')
      case 403:
        return new AppError(AppErrorType.FORBIDDEN, message || '权限不足')
      case 404:
        return new AppError(AppErrorType.NOT_FOUND, message || '请求的资源不存在')
      default:
        if (status >= 500) {
          return new AppError(AppErrorType.SERVER_ERROR, message || '服务器错误')
        }
        return new AppError(AppErrorType.BUSINESS_ERROR, message || '请求失败')
    }
  }

  /**
   * 创建网络错误
   */
  static networkError(): AppError {
    return new AppError(AppErrorType.NETWORK_ERROR, '网络错误，请检查您的网络连接')
  }

  /**
   * 创建超时错误
   */
  static timeoutError(): AppError {
    return new AppError(AppErrorType.TIMEOUT, '请求超时，请稍后重试')
  }

  /**
   * 创建未知错误
   */
  static unknownError(originalError?: unknown): AppError {
    return new AppError(
      AppErrorType.UNKNOWN_ERROR,
      '发生未知错误',
      { originalError },
    )
  }
}
