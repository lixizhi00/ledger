import { post } from '@/utils/request.js'
import { setToken, setUserId } from '@/utils/config.js'

/** 注册：phone, username, password, confirmPassword */
export function register(data) {
  return post('/api/auth/register', data).then(res => {
    if (res.token) {
      setToken(res.token)
      if (res.userId) setUserId(res.userId)
    }
    return res
  })
}

/** 登录：account（手机号或账户名）, password */
export function login(data) {
  return post('/api/auth/login', data).then(res => {
    if (res.token) {
      setToken(res.token)
      if (res.userId) setUserId(res.userId)
    }
    return res
  })
}

/** 忘记密码：phone, newPassword, confirmPassword */
export function resetPassword(data) {
  return post('/api/auth/reset-password', data)
}

/** 微信小程序：用 getPhoneNumber 返回的 code 兑换手机号，返回 { phone } */
export function getWechatPhone(code) {
  return post('/api/auth/wechat-phone', { code })
}
