<template>
  <view class="page">
    <view class="tabs">
      <view
        class="tab"
        :class="{ active: activeTab === 'month' }"
        @click="activeTab = 'month'"
      >
        月预算
      </view>
      <view
        class="tab"
        :class="{ active: activeTab === 'year' }"
        @click="activeTab = 'year'"
      >
        年预算
      </view>
    </view>

    <template v-if="activeTab === 'month'">
      <view class="summary-card">
        <view class="row">
          <text>当前月拥有预算</text>
          <text class="num">¥ {{ monthAvailableTotal.toFixed(2) }}</text>
        </view>
        <view class="row hint">月预算 + 前面月结余（下列一级/二级同）</view>
        <view class="row">
          <text>本月已支出</text>
          <text class="num">¥ {{ monthSpent.toFixed(2) }}</text>
        </view>
        <view class="row" v-if="monthRemain >= 0">
          <text>当前剩余预算</text>
          <text class="num green">¥ {{ monthRemain.toFixed(2) }}</text>
        </view>
        <view class="row" v-else>
          <text>当前超支</text>
          <text class="num red">¥ {{ (-monthRemain).toFixed(2) }}</text>
        </view>
        <view class="progress-wrap">
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: progressPercent + '%' }"
              :class="{ over: monthSpent > monthAvailableTotal }"
            />
          </view>
          <text class="progress-text">{{ progressText }}</text>
        </view>
      </view>
      <view class="section" v-for="parent in budgetTree" :key="parent.id">
        <view class="parent-row">
          <text class="parent-name">{{ parent.name }}</text>
          <text class="input readonly">¥ {{ getCategoryMonthAvailableDisplay(parent.id, parent) }}</text>
        </view>
        <navigator
          class="child-row"
          v-for="child in parent.children"
          :key="child.id"
          :url="'/pages/record-list/record-list?type=year&categoryId=' + child.id"
          open-type="navigate"
        >
          <view class="child-left">
            <text class="child-name">{{ child.name }}</text>
            <view class="progress-wrap small">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: getChildProgressPercent(child.id) + '%' }"
                  :class="{ over: getMonthSpendingByCategory(child.id) > getCategoryMonthAvailable(child.id) }"
                />
              </view>
            </view>
            <text class="child-remain" :class="{ over: getChildRemain(child.id) < 0 }">
              {{ getChildRemain(child.id) >= 0 ? '剩余 ¥' + getChildRemain(child.id).toFixed(2) : '超支 ¥' + (-getChildRemain(child.id)).toFixed(2) }}
            </text>
          </view>
          <view class="child-right">
            <text class="input readonly">¥ {{ getCategoryMonthAvailableDisplay(child.id) }}</text>
            <text class="arrow-link">›</text>
          </view>
        </navigator>
      </view>
    </template>

    <template v-if="activeTab === 'year'">
      <view class="summary-card">
        <view class="row">
          <text>年总预算</text>
          <text class="num">¥ {{ yearTotal.toFixed(2) }}</text>
        </view>
        <view class="row">
          <text>今年已支出</text>
          <text class="num">¥ {{ yearSpent.toFixed(2) }}</text>
        </view>
        <view class="row" v-if="yearRemain >= 0">
          <text>剩余预算</text>
          <text class="num green">¥ {{ yearRemain.toFixed(2) }}</text>
        </view>
        <view class="row" v-else>
          <text>超支</text>
          <text class="num red">¥ {{ (-yearRemain).toFixed(2) }}</text>
        </view>
        <view class="progress-wrap">
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: yearProgressPercent + '%' }"
              :class="{ over: yearSpent > yearTotal }"
            />
          </view>
          <text class="progress-text">{{ yearProgressText }}</text>
        </view>
      </view>
      <view class="section" v-for="parent in budgetTree" :key="parent.id">
        <view class="parent-row">
          <text class="parent-name">{{ parent.name }}</text>
          <text class="input readonly">¥ {{ getParentYearSum(parent) }}</text>
        </view>
        <view class="child-row year-child" v-for="child in parent.children" :key="child.id">
          <text class="child-name">{{ child.name }}</text>
          <view class="year-child-right">
            <text v-if="!yearEditing" class="input readonly">¥ {{ getYearBudgetDisplay(child.id) }}</text>
            <input
              v-else
              class="input"
              type="digit"
              :value="getYearBudget(child.id)"
              @input="e => setYearBudgetDraft(parent, child.id, e.detail.value)"
              placeholder="0"
            />
          </view>
        </view>
      </view>
      <view class="year-bottom-bar">
        <button class="btn-edit-total" @click="onYearEdit">{{ yearEditing ? '取消' : '编辑' }}</button>
        <button class="btn-save" :class="{ primary: yearEditing }" @click="onYearSave">保存</button>
      </view>
    </template>

    <view class="tip">一级预算=其二级之和（只编辑二级）；月预算由年÷12自动得出。</view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { requireAuth } from '@/utils/auth.js'
import { getCategories } from '@/api/categories.js'
import { getBudget, updateYearlyBudget } from '@/api/budget.js'
import { getRecordsByDateRange } from '@/api/records.js'
import { getStatsSummary } from '@/api/stats.js'
import { getMonthStartStr, getTodayStr, getLastMonthStartStr, getLastMonthEndStr } from '@/utils/date.js'

const activeTab = ref('month')
const budgetMonth = ref({})
const budgetYear = ref({})
const categories = ref([])
const recordsThisMonth = ref([])
const recordsLastMonth = ref([])
const monthSpentFromStats = ref(0)
const yearSpentFromStats = ref(0)
const yearEditing = ref(false)

const budgetTree = computed(() => {
  const cats = categories.value
  const parents = cats.filter(c => !c.parentId).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return parents.map(p => ({
    ...p,
    children: cats.filter(c => c.parentId === p.id).sort((a, b) => (a.sortOrder ?? 999) - (b.sortOrder ?? 999))
  }))
})

const monthTotal = computed(() =>
  Object.keys(budgetMonth.value).reduce((s, id) => {
    const c = categories.value.find(x => x.id === id)
    return s + (c && !c.parentId ? (Number(budgetMonth.value[id]) || 0) : 0)
  }, 0)
)
const yearTotal = computed(() =>
  Object.keys(budgetYear.value).reduce((s, id) => {
    const c = categories.value.find(x => x.id === id)
    return s + (c && !c.parentId ? (Number(budgetYear.value[id]) || 0) : 0)
  }, 0)
)

const lastMonthSpent = computed(() =>
  recordsLastMonth.value.reduce((s, r) => s + (Number(r.amount) || 0), 0)
)
const lastMonthBalance = computed(() => monthTotal.value - lastMonthSpent.value)
const monthAvailableTotal = computed(() => monthTotal.value + lastMonthBalance.value)
const monthSpent = computed(() => monthSpentFromStats.value)
const monthRemain = computed(() => monthAvailableTotal.value - monthSpent.value)
const progressPercent = computed(() => {
  if (monthAvailableTotal.value <= 0) return 0
  return Math.min(100, (monthSpent.value / monthAvailableTotal.value) * 100)
})
const progressText = computed(() => {
  if (monthAvailableTotal.value <= 0) return '暂无预算'
  if (monthSpent.value > monthAvailableTotal.value) {
    return `已用 ¥${monthSpent.value.toFixed(0)} / 拥有 ¥${monthAvailableTotal.value.toFixed(0)}，超支 ¥${(monthSpent.value - monthAvailableTotal.value).toFixed(0)}`
  }
  return `已用 ¥${monthSpent.value.toFixed(0)} / 拥有 ¥${monthAvailableTotal.value.toFixed(0)}，剩余 ¥${monthRemain.value.toFixed(0)}`
})

const yearSpent = computed(() => yearSpentFromStats.value)
const yearRemain = computed(() => yearTotal.value - yearSpent.value)
const yearProgressPercent = computed(() => {
  if (yearTotal.value <= 0) return 0
  return Math.min(100, (yearSpent.value / yearTotal.value) * 100)
})
const yearProgressText = computed(() => {
  if (yearTotal.value <= 0) return '暂无预算'
  if (yearSpent.value > yearTotal.value) {
    return `已用 ¥${yearSpent.value.toFixed(0)} / 预算 ¥${yearTotal.value.toFixed(0)}，超支 ¥${(yearSpent.value - yearTotal.value).toFixed(0)}`
  }
  return `已用 ¥${yearSpent.value.toFixed(0)} / 预算 ¥${yearTotal.value.toFixed(0)}，剩余 ¥${yearRemain.value.toFixed(0)}`
})

function getParentYearSum(parent) {
  const sum = parent.children.reduce((s, c) => s + (Number(budgetYear.value[c.id]) || 0), 0)
  return sum.toFixed(2)
}
function getMonthBudget(id) {
  const v = budgetMonth.value[id]
  return v === undefined || v === '' ? '' : String(v)
}
function getMonthBudgetDisplay(id) {
  const v = budgetMonth.value[id]
  const n = v === undefined || v === '' ? 0 : Number(v)
  return n.toFixed(2)
}
function getMonthBudgetNum(id) {
  const v = budgetMonth.value[id]
  return v === undefined || v === '' ? 0 : Number(v)
}
function getSpendingByCategoryInRange(records, categoryId) {
  return records
    .filter(r => r.categoryId === categoryId)
    .reduce((s, r) => s + (Number(r.amount) || 0), 0)
}
/** 某分类上月结余（当月预算 - 上月该分类支出）。二级：按该二级支出算；一级：按其下所有二级支出之和算 */
function getCategoryLastMonthBalance(catId, parent) {
  const base = getMonthBudgetNum(catId)
  let lastSpent
  if (parent && parent.id === catId) {
    lastSpent = parent.children.reduce(
      (s, c) => s + getSpendingByCategoryInRange(recordsLastMonth.value, c.id),
      0
    )
  } else {
    lastSpent = getSpendingByCategoryInRange(recordsLastMonth.value, catId)
  }
  return base - lastSpent
}
/** 某分类当前月拥有额度 = 当月额度 + 上月剩余（一级=当月一级预算+上月一级剩余，二级=当月二级+上月二级剩余） */
function getCategoryMonthAvailable(catId, parent) {
  return getMonthBudgetNum(catId) + getCategoryLastMonthBalance(catId, parent)
}
function getCategoryMonthAvailableDisplay(id, parent) {
  return getCategoryMonthAvailable(id, parent).toFixed(2)
}
function getMonthSpendingByCategory(categoryId) {
  return getSpendingByCategoryInRange(recordsThisMonth.value, categoryId)
}
function getChildProgressPercent(childId) {
  const available = getCategoryMonthAvailable(childId)
  if (available <= 0) return 0
  const spent = getMonthSpendingByCategory(childId)
  return Math.min(100, (spent / available) * 100)
}
function getChildRemain(childId) {
  const available = getCategoryMonthAvailable(childId)
  const spent = getMonthSpendingByCategory(childId)
  return available - spent
}
function getYearBudget(id) {
  const v = budgetYear.value[id]
  return v === undefined || v === '' ? '' : String(v)
}
function getYearBudgetDisplay(id) {
  const v = budgetYear.value[id]
  const n = v === undefined || v === '' ? 0 : Number(v)
  return n.toFixed(2)
}
function setYearBudgetDraft(parent, childId, val) {
  const num = parseFloat(val)
  const yearVal = isNaN(num) ? 0 : num
  budgetYear.value = { ...budgetYear.value, [childId]: yearVal }
  const parentYearSum = parent.children.reduce(
    (s, c) => s + (Number(budgetYear.value[c.id]) || 0),
    0
  )
  budgetYear.value = { ...budgetYear.value, [parent.id]: parentYearSum }
}
async function saveYearBudget() {
  const newYearly = { ...budgetYear.value }
  try {
    const res = await updateYearlyBudget(newYearly)
    budgetYear.value = { ...(res.yearly || {}) }
    budgetMonth.value = { ...(res.monthly || {}) }
    yearEditing.value = false
    uni.showToast({ title: '已保存' })
  } catch (e) {}
}
function onYearEdit() {
  if (yearEditing.value) {
    yearEditing.value = false
  } else {
    yearEditing.value = true
  }
}
function onYearSave() {
  if (yearEditing.value) {
    saveYearBudget()
  } else {
    uni.showToast({ title: '请先点击编辑修改预算', icon: 'none' })
  }
}

onMounted(async () => {
  try {
    const [cats, budgetRes, statsRes, recsThis, recsLast] = await Promise.all([
      getCategories(),
      getBudget(),
      getStatsSummary(),
      getRecordsByDateRange(getMonthStartStr(), getTodayStr()),
      getRecordsByDateRange(getLastMonthStartStr(), getLastMonthEndStr())
    ])
    categories.value = cats || []
    budgetYear.value = { ...(budgetRes.yearly || {}) }
    budgetMonth.value = { ...(budgetRes.monthly || {}) }
    monthSpentFromStats.value = statsRes.month ?? 0
    yearSpentFromStats.value = statsRes.year ?? 0
    recordsThisMonth.value = recsThis || []
    recordsLastMonth.value = recsLast || []
    syncParentBudgets()
  } catch {
    categories.value = []
    budgetYear.value = {}
    budgetMonth.value = {}
    recordsThisMonth.value = []
    recordsLastMonth.value = []
  }
})
onShow(() => { requireAuth() })

function syncParentBudgets() {
  const tree = budgetTree.value
  tree.forEach(parent => {
    const sum = parent.children.reduce((s, c) => s + (Number(budgetYear.value[c.id]) || 0), 0)
    budgetYear.value = { ...budgetYear.value, [parent.id]: sum }
    budgetMonth.value = { ...budgetMonth.value, [parent.id]: sum / 12 }
  })
}
</script>

<style scoped>
.page { padding: 40rpx 32rpx; padding-bottom: 60rpx; }
.tabs {
  display: flex;
  margin-bottom: 28rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(59, 130, 246, 0.06);
}
.tab {
  flex: 1; text-align: center; padding: 28rpx; font-size: 30rpx; color: #64748b;
}
.tab.active {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-weight: 600;
}
.summary-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 36rpx 32rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.06);
}
.summary-card .row { display: flex; justify-content: space-between; align-items: center; padding: 14rpx 0; font-size: 28rpx; color: #475569; }
.summary-card .row.hint { font-size: 24rpx; color: #94a3b8; padding-top: 0; }
.summary-card .num { font-weight: 600; color: #3b82f6; font-size: 34rpx; }
.summary-card .num.green { color: #10b981; }
.summary-card .num.red { color: #ef4444; }
.progress-wrap { margin-top: 24rpx; }
.progress-bar { height: 20rpx; background: #f1f5f9; border-radius: 10rpx; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #3b82f6, #60a5fa); border-radius: 10rpx; transition: width 0.2s; }
.progress-fill.over { background: linear-gradient(90deg, #ef4444, #f87171); }
.progress-text { display: block; font-size: 24rpx; color: #64748b; margin-top: 12rpx; }
.section {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx 32rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.06);
}
.parent-row { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #f1f5f9; }
.parent-name { font-size: 32rpx; font-weight: 600; color: #1e293b; }
.child-row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0 20rpx 24rpx; text-decoration: none; color: inherit; }
.child-row:active { background: #f8fafc; }
.child-left { flex: 1; min-width: 0; }
.child-name { display: block; font-size: 28rpx; color: #475569; margin-bottom: 8rpx; }
.progress-wrap.small { margin-top: 6rpx; }
.progress-wrap.small .progress-bar { height: 10rpx; }
.child-remain { display: block; font-size: 24rpx; color: #10b981; margin-top: 8rpx; }
.child-remain.over { color: #ef4444; }
.child-right { display: flex; align-items: center; gap: 16rpx; }
.arrow-link { font-size: 36rpx; color: #3b82f6; }
.input { width: 140rpx; text-align: right; font-size: 28rpx; color: #1e293b; }
.input.readonly { border: none; background: transparent; color: #475569; }
.child-row.year-child .child-name { flex: 1; }
.year-child-right { display: flex; align-items: center; }
.year-bottom-bar {
  display: flex;
  gap: 24rpx;
  margin-top: 28rpx;
  padding-bottom: 24rpx;
}
.year-bottom-bar .btn-edit-total {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 28rpx;
  color: #3b82f6;
  font-weight: 500;
  background: rgba(59, 130, 246, 0.12);
  border-radius: 16rpx;
  border: none;
}
.year-bottom-bar .btn-edit-total:active { opacity: 0.85; }
.year-bottom-bar .btn-save {
  flex: 1;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 16rpx;
  border: none;
  background: #f1f5f9;
  color: #64748b;
}
.year-bottom-bar .btn-save.primary {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
}
.year-bottom-bar .btn-save:active { opacity: 0.92; }
.tip { font-size: 24rpx; color: #94a3b8; text-align: center; margin-top: 28rpx; }
</style>
