<template>
  <view class="page">
    <view class="filter-bar loading" v-if="loading">加载中...</view>
    <view class="filter-bar" v-else-if="filterLabel">
      <text class="filter-label">{{ filterLabel }}</text>
    </view>
    <view class="list" v-if="list.length">
      <view class="item" v-for="r in list" :key="r.id">
        <view class="row main">
          <text class="amount">¥ {{ (r.amount || 0).toFixed(2) }}</text>
          <text class="date">{{ r.date }}</text>
        </view>
        <view class="row sub">
          <text class="category">{{ categoryName(r.categoryId) }}</text>
          <text class="note" v-if="r.note">{{ r.note }}</text>
        </view>
      </view>
    </view>
    <view class="empty" v-else>
      <text>暂无账目</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { requireAuth } from '@/utils/auth.js'
import { getRecordsByDateRange } from '@/api/records.js'
import { getCategories } from '@/api/categories.js'
import { getTodayStr, getMonthStartStr, getYearStartStr, getMonthEndStr } from '@/utils/date.js'

const type = ref('') // today | month | year
const categoryId = ref('')
const filterLabel = ref('')
const list = ref([])
const categories = ref([])
const loading = ref(false)

onShow(() => { requireAuth() })

onLoad(async (opts) => {
  if (!requireAuth()) return
  type.value = opts.type || ''
  categoryId.value = opts.categoryId || ''
  let start = ''
  let end = ''
  let label = ''
  if (type.value === 'today') {
    start = end = getTodayStr()
    label = '今日支出'
  } else if (type.value === 'month') {
    start = getMonthStartStr()
    end = getMonthEndStr()
    label = '本月支出'
  } else if (type.value === 'year') {
    start = getYearStartStr()
    end = getTodayStr()
    label = '今年至今支出'
  } else {
    start = '2000-01-01'
    end = getTodayStr()
    label = '全部'
  }
  loading.value = true
  try {
    const [catList, records] = await Promise.all([
      getCategories(),
      getRecordsByDateRange(start, end, categoryId.value || undefined)
    ])
    categories.value = catList || []
    list.value = records || []
    if (categoryId.value) {
      const catName = getCategoryDisplayName(categoryId.value, catList || [])
      filterLabel.value = label + (catName ? ' - ' + catName : '')
    } else {
      filterLabel.value = label
    }
  } catch {
    list.value = []
    categories.value = []
    filterLabel.value = label
  } finally {
    loading.value = false
  }
})

function getCategoryDisplayName(id, cats) {
  const list = cats || categories.value
  if (!id) return ''
  const c = list.find(x => x.id === id)
  if (!c) return ''
  if (c.parentId) {
    const p = list.find(x => x.id === c.parentId)
    return p ? p.name + ' > ' + c.name : c.name
  }
  return c.name
}

function categoryName(id) {
  const s = getCategoryDisplayName(id, categories.value)
  return s || '未分类'
}
</script>

<style scoped>
.page { padding: 40rpx 32rpx; padding-bottom: 60rpx; }
.filter-bar {
  margin-bottom: 28rpx;
  padding: 20rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #475569;
  box-shadow: 0 4rpx 20rpx rgba(59, 130, 246, 0.06);
}
.filter-label { font-weight: 600; color: #1e293b; }
.list {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.06);
}
.item {
  padding: 36rpx 28rpx;
  border-bottom: 1rpx solid #f1f5f9;
}
.item:last-child { border-bottom: none; }
.row { display: flex; justify-content: space-between; align-items: center; }
.main { margin-bottom: 14rpx; }
.amount { font-size: 36rpx; font-weight: 700; color: #1e293b; }
.date { font-size: 26rpx; color: #94a3b8; }
.sub { font-size: 26rpx; color: #64748b; }
.note { margin-left: 20rpx; color: #94a3b8; max-width: 300rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty {
  text-align: center;
  padding: 100rpx;
  color: #94a3b8;
  font-size: 28rpx;
  background: #fff;
  border-radius: 24rpx;
  margin-top: 28rpx;
}
</style>
