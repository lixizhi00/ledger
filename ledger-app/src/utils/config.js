/** API 基础地址，可通过 .env 配置 */
export function getApiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL
}

/** 当前用户 ID，用于多租户，默认 default */
const USER_ID_KEY = 'ledger_user_id'
const TOKEN_KEY = 'ledger_token'

export function getUserId() {
  try {
    const id = uni.getStorageSync(USER_ID_KEY)
    return id && String(id).trim() ? id.trim() : 'default'
  } catch {
    return 'default'
  }
}
export function setUserId(id) {
  try {
    uni.setStorageSync(USER_ID_KEY, id ? String(id).trim() : 'default')
  } catch (e) {}
}

export function getToken() {
  try {
    const t = uni.getStorageSync(TOKEN_KEY)
    return t && String(t).trim() ? t.trim() : ''
  } catch {
    return ''
  }
}
export function setToken(token) {
  try {
    uni.setStorageSync(TOKEN_KEY, token ? String(token).trim() : '')
  } catch (e) {}
}
export function clearAuth() {
  try {
    uni.removeStorageSync(TOKEN_KEY)
    uni.removeStorageSync(USER_ID_KEY)
  } catch (e) {}
}
