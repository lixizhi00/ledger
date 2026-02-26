<template>
  <view class="page">
    <view class="stats-card">
      <view class="stat-item" @click="goRecordList('year')">
        <view class="stat-left">
          <text class="label">今年至今总支出</text>
          <text class="date-hint">{{ dateRangeYear }}</text>
          <text class="value">¥ {{ yearTotal }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="stat-item" @click="goRecordList('month')">
        <view class="stat-left">
          <text class="label">本月总支出</text>
          <text class="date-hint">{{ dateRangeMonth }}</text>
          <text class="value">¥ {{ monthTotal }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="stat-item" @click="goRecordList('today')">
        <view class="stat-left">
          <text class="label">今日总支出</text>
          <text class="date-hint">{{ dateRangeToday }}</text>
          <text class="value">¥ {{ dayTotal }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>
    <view class="entry-wrap">
      <navigator class="entry-item" url="/pages/budget/budget" open-type="navigate">
        <text class="entry-text">预算</text>
      </navigator>
      <navigator class="entry-item" url="/pages/record/record" open-type="navigate">
        <text class="entry-text">记账</text>
      </navigator>
      <navigator class="entry-item" url="/pages/category/category" open-type="navigate">
        <text class="entry-text">分类</text>
      </navigator>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { requireAuth } from '@/utils/auth.js'
import { getStatsSummary } from '@/api/stats.js'
import { getTodayStr, getYearStartStr, getMonthStartStr, formatDateCN, formatYearMonthCN } from '@/utils/date.js'

const dayTotal = ref('0.00')
const monthTotal = ref('0.00')
const yearTotal = ref('0.00')
const dateRangeYear = computed(() => {
  const start = formatDateCN(getYearStartStr())
  const end = formatDateCN(getTodayStr())
  return `${start}-${end}`
})
const dateRangeMonth = computed(() => formatYearMonthCN())
const dateRangeToday = computed(() => formatDateCN(getTodayStr()))

async function refresh() {
  try {
    const res = await getStatsSummary()
    dayTotal.value = (res.today ?? 0).toFixed(2)
    monthTotal.value = (res.month ?? 0).toFixed(2)
    yearTotal.value = (res.year ?? 0).toFixed(2)
  } catch {
    dayTotal.value = '0.00'
    monthTotal.value = '0.00'
    yearTotal.value = '0.00'
  }
}

onMounted(refresh)
onShow(() => {
  if (!requireAuth()) return
  refresh()
})

function goRecordList(type) {
  uni.navigateTo({ url: '/pages/record-list/record-list?type=' + type })
}
</script>

<style scoped>
.page {
  padding: 40rpx 32rpx;
  min-height: 100vh;
}
.stats-card {
  background: linear-gradient(145deg, #3b82f6 0%, #2563eb 50%, #1d4ed8 100%);
  border-radius: 28rpx;
  padding: 44rpx 40rpx;
  color: #fff;
  margin-bottom: 48rpx;
  box-shadow: 0 12rpx 40rpx rgba(59, 130, 246, 0.35);
}
.stat-item {
  margin-bottom: 28rpx;
  padding: 12rpx 0;
  border-radius: 12rpx;
}
.stat-item:last-child { margin-bottom: 0; }
.stat-item { display: flex; align-items: center; justify-content: space-between; }
.stat-item:active { background: rgba(255,255,255,0.1); }
.stat-left { flex: 1; }
.arrow { font-size: 36rpx; opacity: 0.85; margin-left: 20rpx; }
.label {
  display: block;
  font-size: 28rpx;
  opacity: 0.92;
  margin-bottom: 6rpx;
}
.date-hint {
  display: block;
  font-size: 24rpx;
  opacity: 0.85;
  margin-bottom: 10rpx;
}
.value {
  font-size: 44rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}
.entry-wrap {
  display: flex;
  gap: 28rpx;
  justify-content: center;
  flex-wrap: wrap;
}
.entry-item {
  width: 200rpx;
  height: 200rpx;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(59, 130, 246, 0.12);
  transition: transform 0.2s, box-shadow 0.2s;
}
.entry-item:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 20rpx rgba(59, 130, 246, 0.1);
}
.entry-text {
  font-size: 32rpx;
  color: #1e293b;
  font-weight: 600;
}
</style>
