import { getToken } from './config.js'

/** 白名单：无需登录即可访问的页面 route（如 pages/login/login） */
const ALLOWED_ROUTES = ['pages/login/login', 'pages/register/register', 'pages/forget-password/forget-password']

/**
 * 要求已登录：未登录则 reLaunch 到登录页
 * @returns {boolean} 是否已登录（true 可继续，false 已跳转登录）
 */
export function requireAuth() {
  if (getToken()) return true
  const pages = getCurrentPages()
  const cur = pages[pages.length - 1]
  const route = (cur && cur.route) ? cur.route : ''
  if (ALLOWED_ROUTES.includes(route)) return true
  uni.reLaunch({ url: '/pages/login/login' })
  return false
}
