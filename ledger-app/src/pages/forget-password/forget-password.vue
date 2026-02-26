<template>
  <view class="page">
    <view class="bg">
      <view class="bg-gradient" />
      <view class="bg-blur bg-blur-1" />
      <view class="bg-blur bg-blur-2" />
      <view class="bg-blur bg-blur-3" />
    </view>
    <view class="content">
      <view class="header">
        <text class="title">忘记密码</text>
        <text class="subtitle">输入注册手机号，设置新密码</text>
      </view>
      <view class="form">
        <view class="field">
          <input
            class="input"
            type="number"
            v-model="phone"
            placeholder="请输入注册手机号"
            placeholder-class="placeholder"
          />
        </view>
        <view class="field field-pwd">
            <input
              class="input"
              type="text"
              :password="!showPwd"
              v-model="newPassword"
            placeholder="新密码（6位以上，数字/字母/字符至少两种）"
            placeholder-class="placeholder"
          />
          <text class="pwd-toggle" :aria-label="showPwd ? '隐藏密码' : '显示密码'" @click="showPwd = !showPwd">👁</text>
        </view>
        <view class="field field-pwd">
            <input
              class="input"
              type="text"
              :password="!showConfirmPwd"
              v-model="confirmPassword"
            placeholder="确认新密码"
            placeholder-class="placeholder"
          />
          <text class="pwd-toggle" :aria-label="showConfirmPwd ? '隐藏密码' : '显示密码'" @click="showConfirmPwd = !showConfirmPwd">👁</text>
        </view>
        <button class="btn-save" :disabled="loading" @click="onSave">
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </view>
      <view class="footer">
        <text class="footer-text">想起密码？</text>
        <text class="footer-link" @click="goLogin">去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { resetPassword } from '@/api/auth.js'

const phone = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const showPwd = ref(false)
const showConfirmPwd = ref(false)
const loading = ref(false)

async function onSave() {
  const p = (phone.value || '').trim()
  const np = (newPassword.value || '').trim()
  const cp = (confirmPassword.value || '').trim()
  if (!p) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!np) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  if (np !== cp) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    await resetPassword({ phone: p, newPassword: np, confirmPassword: cp })
    uni.showToast({ title: '密码已重置，请登录' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/login/login' })
    }, 500)
  } catch (e) {
    // toast 已在 request 中处理
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}
</script>

<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  box-sizing: border-box;
}
.bg {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 0;
}
.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 35%, #24243e 60%, #1a1a2e 100%);
}
.bg-blur {
  position: absolute;
  border-radius: 50%;
  filter: blur(80rpx);
  opacity: 0.4;
}
.bg-blur-1 {
  width: 400rpx;
  height: 400rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100rpx;
  right: -80rpx;
}
.bg-blur-2 {
  width: 320rpx;
  height: 320rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: 20%;
  left: -60rpx;
}
.bg-blur-3 {
  width: 280rpx;
  height: 280rpx;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  bottom: -80rpx;
  right: 20%;
}
.content {
  position: relative;
  z-index: 1;
  padding: 80rpx 48rpx 60rpx;
}
.header {
  margin-bottom: 48rpx;
}
.title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.2);
}
.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.85);
}
.form {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx 40rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.06);
}
.field {
  margin-bottom: 28rpx;
}
.field:last-of-type {
  margin-bottom: 40rpx;
}
.field-pwd {
  position: relative;
}
.pwd-toggle {
  position: absolute;
  right: 28rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40rpx;
  line-height: 1;
  opacity: 0.9;
}
.pwd-toggle:active { opacity: 0.8; }
.input {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  padding: 0 28rpx;
  font-size: 30rpx;
  color: #1e293b;
  background: #f8fafc;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  box-sizing: border-box;
}
.input:focus {
  border-color: #3b82f6;
  background: #fff;
}
.placeholder {
  color: #94a3b8;
}
.btn-save {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 16rpx;
}
.btn-save[disabled] {
  opacity: 0.7;
}
.btn-save:active:not([disabled]) {
  opacity: 0.9;
}
.footer {
  margin-top: 56rpx;
  text-align: center;
}
.footer-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}
.footer-link {
  font-size: 28rpx;
  color: #a5b4fc;
  margin-left: 8rpx;
  font-weight: 500;
}
.footer-link:active {
  opacity: 0.8;
}
</style>
