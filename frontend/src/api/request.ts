import Taro from '@tarojs/taro'
import type { ApiResponse } from '@/types/api'

const BASE_URL = process.env.TARO_APP_API_BASE_URL || 'http://localhost:3000/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: Record<string, unknown>
  header?: Record<string, string>
}

async function request<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  const token = Taro.getStorageSync('access_token')

  const header: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.header,
  }

  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  try {
    const res = await Taro.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
    })

    if (res.statusCode === 401) {
      // Token expired, try refresh
      const refreshToken = Taro.getStorageSync('refresh_token')
      if (refreshToken) {
        try {
          const refreshRes = await Taro.request({
            url: `${BASE_URL}/auth/refresh`,
            method: 'POST',
            data: { refresh_token: refreshToken },
            header: { 'Content-Type': 'application/json' },
          })

          if (refreshRes.statusCode === 200) {
            const data = refreshRes.data as ApiResponse<{ access_token: string }>
            if (data.data?.access_token) {
              Taro.setStorageSync('access_token', data.data.access_token)
              // Retry original request
              header['Authorization'] = `Bearer ${data.data.access_token}`
              const retryRes = await Taro.request({
                url: `${BASE_URL}${options.url}`,
                method: options.method || 'GET',
                data: options.data,
                header,
              })
              return retryRes.data as ApiResponse<T>
            }
          }
        } catch {
          // Refresh failed, redirect to login
          Taro.removeStorageSync('access_token')
          Taro.removeStorageSync('refresh_token')
          Taro.navigateTo({ url: '/pages/client/login' })
        }
      } else {
        Taro.navigateTo({ url: '/pages/client/login' })
      }
    }

    return res.data as ApiResponse<T>
  } catch (err) {
    return {
      code: 500,
      message: err instanceof Error ? err.message : 'Network error',
    }
  }
}

export const api = {
  get: <T>(url: string) => request<T>({ url, method: 'GET' }),
  post: <T>(url: string, data?: Record<string, unknown>) =>
    request<T>({ url, method: 'POST', data }),
  put: <T>(url: string, data?: Record<string, unknown>) =>
    request<T>({ url, method: 'PUT', data }),
  delete: <T>(url: string) => request<T>({ url, method: 'DELETE' }),
}
