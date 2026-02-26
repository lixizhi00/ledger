<template>
  <view class="page">
    <view class="form">
      <view class="row">
        <text class="label">金额</text>
        <input
          class="input amount"
          type="digit"
          v-model="amount"
          placeholder="0.00"
          focus
        />
      </view>
      <picker
        mode="multiSelector"
        :range="cascadeRange"
        range-key="name"
        :value="cascadeValue"
        @change="onCascadeChange"
        @columnchange="onCascadeColumnChange"
      >
        <view class="row">
          <text class="label">分类</text>
          <text class="value">{{ currentCategoryName || '请选择' }}</text>
          <text class="arrow">›</text>
        </view>
      </picker>
      <picker mode="date" :value="date" @change="onDateChange">
        <view class="row">
          <text class="label">时间</text>
          <text class="value">{{ date }}</text>
          <text class="arrow">›</text>
        </view>
      </picker>
      <view class="row">
        <text class="label">备注</text>
        <input
          class="input"
          v-model="note"
          placeholder="选填"
        />
      </view>
      <button class="btn" @click="submit">完成</button>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { requireAuth } from '@/utils/auth.js'
import { getCategories } from '@/api/categories.js'
import { addRecord } from '@/api/records.js'
import { formatDate } from '@/utils/date.js'

const amount = ref('')
const categoryId = ref('')
const date = ref(formatDate(new Date()))
const note = ref('')
const loading = ref(false)

const categories = ref([])
// 级联：一级 + 二级（按一级分组）
const categoryTree = computed(() => {
  const parents = categories.value.filter(c => !c.parentId).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return parents.map(p => ({
    ...p,
    children: categories.value
      .filter(c => c.parentId === p.id)
      .sort((a, b) => (a.sortOrder ?? 999) - (b.sortOrder ?? 999))
  }))
})
const cascadeRange = computed(() => {
  const tree = categoryTree.value
  const col0 = tree.map(p => ({ name: p.name, id: p.id }))
  const pIdx = Math.min(cascadeValue.value[0], tree.length - 1)
  const col1 = pIdx >= 0 && tree[pIdx] ? tree[pIdx].children.map(c => ({ name: c.name, id: c.id })) : []
  return [col0, col1]
})
const cascadeValue = ref([0, 0])
const currentCategoryName = computed(() => {
  const tree = categoryTree.value
  const [pi, ci] = cascadeValue.value
  if (pi < 0 || !tree[pi]) return ''
  const child = tree[pi].children[ci]
  if (!child) return tree[pi].children[0] ? tree[pi].name + ' > ' + tree[pi].children[0].name : ''
  return tree[pi].name + ' > ' + child.name
})

watch(
  () => [categories.value, categoryId.value],
  () => {
    const tree = categoryTree.value
    if (!tree.length || !categoryId.value) return
    for (let pi = 0; pi < tree.length; pi++) {
      const ci = tree[pi].children.findIndex(c => c.id === categoryId.value)
      if (ci >= 0) {
        cascadeValue.value = [pi, ci]
        return
      }
    }
  },
  { immediate: true }
)
onMounted(async () => {
  try {
    categories.value = await getCategories()
    const tree = categoryTree.value
    if (tree.length && !categoryId.value && tree[0].children && tree[0].children[0]) {
      categoryId.value = tree[0].children[0].id
      cascadeValue.value = [0, 0]
    }
  } catch {
    categories.value = []
  }
})
onShow(() => { requireAuth() })

function onDateChange(e) {
  date.value = e.detail.value
}

function onCascadeChange(e) {
  const val = e.detail.value
  const [pi, ci] = Array.isArray(val) ? val : [0, 0]
  cascadeValue.value = [pi, ci]
  const tree = categoryTree.value
  if (tree[pi] && tree[pi].children && tree[pi].children[ci]) {
    categoryId.value = tree[pi].children[ci].id
  }
}
function onCascadeColumnChange(e) {
  const col = e.detail.column
  const idx = e.detail.value
  if (col === 0) {
    cascadeValue.value = [idx, 0]
    const tree = categoryTree.value
    if (tree[idx] && tree[idx].children && tree[idx].children[0]) {
      categoryId.value = tree[idx].children[0].id
    }
  } else {
    const [pi] = cascadeValue.value
    cascadeValue.value = [pi, idx]
    const tree = categoryTree.value
    if (tree[pi] && tree[pi].children && tree[pi].children[idx]) {
      categoryId.value = tree[pi].children[idx].id
    }
  }
}

async function submit() {
  const trimAmount = (amount.value || '').trim()
  if (!trimAmount) {
    uni.showToast({ title: '请输入支出金额', icon: 'none' })
    return
  }
  const num = parseFloat(trimAmount)
  if (isNaN(num) || num <= 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  if (!categoryId.value) {
    uni.showToast({ title: '请选择分类', icon: 'none' })
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    await addRecord({
      amount: num,
      categoryId: categoryId.value,
      date: date.value,
      note: note.value.trim()
    })
    uni.showToast({ title: '已记一笔' })
    setTimeout(() => uni.navigateBack(), 500)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { padding: 40rpx 32rpx; }
.form {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 36rpx;
  box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.06);
}
.row {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}
.row:last-of-type { border-bottom: none; }
.label { width: 120rpx; font-size: 30rpx; color: #475569; }
.input { flex: 1; font-size: 30rpx; color: #1e293b; }
.amount { font-size: 44rpx; font-weight: 700; color: #3b82f6; }
.value { flex: 1; font-size: 30rpx; color: #64748b; }
.arrow { color: #94a3b8; font-size: 36rpx; }
.btn {
  margin-top: 56rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  height: 96rpx;
  line-height: 96rpx;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.3);
}
.btn:active { opacity: 0.92; }
</style>
