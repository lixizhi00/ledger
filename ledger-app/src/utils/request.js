import { getApiBaseUrl } from './config.js'
import { getUserId, getToken } from './config.js'

/**
 * 封装 uni.request，统一 baseURL、Authorization/X-User-Id、错误处理
 */
export function request(options) {
  const baseUrl = getApiBaseUrl()
  if (!baseUrl) {
    return Promise.reject(new Error('未配置 API 地址'))
  }
  const url = options.url.startsWith('http') ? options.url : baseUrl.replace(/\/$/, '') + (options.url.startsWith('/') ? options.url : '/' + options.url)
  const headers = {
    'Content-Type': 'application/json',
    'X-User-Id': getUserId(),
    ...options.header
  }
  const token = getToken()
  if (token) headers['Authorization'] = 'Bearer ' + token
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url,
      header: headers,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const msg = res.data?.message || res.data?.errors || `请求失败 ${res.statusCode}`
          uni.showToast({ title: String(msg).slice(0, 30), icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function get(url, data) {
  return request({ method: 'GET', url, data })
}

export function post(url, data) {
  return request({ method: 'POST', url, data })
}

export function put(url, data) {
  return request({ method: 'PUT', url, data })
}

export function patch(url, data) {
  return request({ method: 'PATCH', url, data })
}

export function del(url) {
  return request({ method: 'DELETE', url })
}
