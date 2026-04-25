export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data?: T
}

export interface PaginatedResponse<T = unknown> {
  code: number
  message: string
  data: T[]
  total: number
  page: number
  page_size: number
}

export interface UserInfo {
  id: string
  username: string
  role: 'admin' | 'dealer' | 'customer'
  avatar?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  access_token: string
  refresh_token: string
  user: UserInfo
}
