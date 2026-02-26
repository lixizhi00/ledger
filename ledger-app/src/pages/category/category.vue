<template>
  <view class="page">
    <view class="list">
      <view class="parent-item" v-for="(parent, pIdx) in tree" :key="parent.id">
        <view class="parent-row">
          <text class="parent-name">{{ parent.name }}</text>
          <view class="parent-actions">
            <text class="btn-add" @click="openAdd(parent.id, null)">+ 添加二级</text>
            <text class="btn-del" @click="deleteCategory(parent.id, true)">删除</text>
          </view>
        </view>
        <view class="children-hint" v-if="parent.children.length">二级分类可拖动排序</view>
        <view class="children">
          <view
            class="child-item"
            v-for="(child, cIdx) in parent.children"
            :key="child.id"
            @touchstart="onChildTouchStart($event, pIdx, cIdx)"
            @touchmove="onChildTouchMove($event, pIdx, cIdx)"
            @touchend="onChildTouchEnd"
          >
            <text class="drag-handle">⋮⋮</text>
            <text class="child-name">{{ child.name }}</text>
            <view class="actions">
              <text class="btn" @click.stop="openAdd(parent.id, child)">编辑</text>
              <text class="btn-del" @click.stop="deleteCategory(child.id, false)">删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <view class="bottom-bar">
      <button class="btn-add-parent" @click="openAdd(null, null)">+ 添加一级分类</button>
    </view>

    <view class="mask" v-if="showModal" @click="showModal = false" />
    <view class="modal" v-if="showModal">
      <view class="modal-title">{{ editingParentId === null ? '一级分类' : '二级分类' }}</view>
      <input
        ref="modalInputRef"
        class="modal-input"
        v-model="editName"
        :focus="showModal"
        :placeholder="editingParentId === null ? '分类名称' : '分类名称'"
      />
      <view class="modal-btns">
        <button class="modal-btn cancel" @click="showModal = false">取消</button>
        <button class="modal-btn ok" @click="doSaveCategory">保存</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { requireAuth } from '@/utils/auth.js'
import { getCategories, saveCategory, deleteCategory as apiDeleteCategory, updateCategorySort } from '@/api/categories.js'

const categories = ref([])
const loading = ref(false)
const showModal = ref(false)
const editName = ref('')
const editingParentId = ref(null)
const editingChild = ref(null)
const dragState = ref({ startY: 0, pIdx: -1, cIdx: -1 })
const DRAG_THRESHOLD = 40

const tree = computed(() => {
  const list = categories.value.filter(c => !c.parentId).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return list.map(p => ({
    ...p,
    children: categories.value
      .filter(c => c.parentId === p.id)
      .sort((a, b) => (a.sortOrder ?? 999) - (b.sortOrder ?? 999))
  }))
})

onMounted(async () => {
  try {
    categories.value = await getCategories()
  } catch {
    categories.value = []
  }
})
onShow(() => { requireAuth() })

function openAdd(parentId, child) {
  editingParentId.value = parentId
  editingChild.value = child
  editName.value = child ? child.name : ''
  showModal.value = true
}

async function doSaveCategory() {
  const name = (editName.value || '').trim()
  if (!name) {
    uni.showToast({ title: '请输入名称', icon: 'none' })
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    if (editingParentId.value === null) {
      const maxOrder = Math.max(0, ...categories.value.filter(c => !c.parentId).map(c => c.sortOrder || 0))
      const saved = await saveCategory({ name, parentId: null, sortOrder: maxOrder + 1 })
      categories.value = [...categories.value, saved]
    } else if (editingChild.value) {
      const saved = await saveCategory({
        id: editingChild.value.id,
        name,
        parentId: editingChild.value.parentId,
        sortOrder: editingChild.value.sortOrder
      })
      categories.value = categories.value.map(c => (c.id === saved.id ? saved : c))
    } else {
      const siblings = categories.value.filter(c => c.parentId === editingParentId.value)
      const maxOrder = Math.max(-1, ...siblings.map(c => c.sortOrder ?? -1))
      const saved = await saveCategory({
        name,
        parentId: editingParentId.value,
        sortOrder: maxOrder + 1
      })
      categories.value = [...categories.value, saved]
    }
    showModal.value = false
    editName.value = ''
    uni.showToast({ title: '已保存' })
  } finally {
    loading.value = false
  }
}

async function moveUp(pIdx, cIdx) {
  if (cIdx <= 0) return
  const parent = tree.value[pIdx]
  const children = [...parent.children]
  const a = children[cIdx - 1].sortOrder ?? cIdx - 1
  const b = children[cIdx].sortOrder ?? cIdx
  try {
    await Promise.all([
      updateCategorySort(children[cIdx - 1].id, b),
      updateCategorySort(children[cIdx].id, a)
    ])
    categories.value = categories.value.map(c => {
      if (c.id === children[cIdx - 1].id) return { ...c, sortOrder: b }
      if (c.id === children[cIdx].id) return { ...c, sortOrder: a }
      return c
    })
  } catch (e) {}
}

async function moveDown(pIdx, cIdx) {
  const parent = tree.value[pIdx]
  if (cIdx >= parent.children.length - 1) return
  const children = [...parent.children]
  const a = children[cIdx].sortOrder ?? cIdx
  const b = children[cIdx + 1].sortOrder ?? cIdx + 1
  try {
    await Promise.all([
      updateCategorySort(children[cIdx].id, b),
      updateCategorySort(children[cIdx + 1].id, a)
    ])
    categories.value = categories.value.map(c => {
      if (c.id === children[cIdx].id) return { ...c, sortOrder: b }
      if (c.id === children[cIdx + 1].id) return { ...c, sortOrder: a }
      return c
    })
  } catch (e) {}
}

function onChildTouchStart(e, pIdx, cIdx) {
  const t = e.touches && e.touches[0]
  if (t) dragState.value = { startY: t.clientY, pIdx, cIdx }
}
function onChildTouchMove(e, pIdx, cIdx) {
  const t = e.touches && e.touches[0]
  if (!t) return
  const state = dragState.value
  const useIdx = state.pIdx >= 0 ? state : { pIdx, cIdx }
  const dy = t.clientY - (state.startY || t.clientY)
  if (dy < -DRAG_THRESHOLD) {
    moveUp(useIdx.pIdx, useIdx.cIdx)
    dragState.value = { startY: t.clientY, pIdx: useIdx.pIdx, cIdx: useIdx.cIdx - 1 }
  } else if (dy > DRAG_THRESHOLD) {
    moveDown(useIdx.pIdx, useIdx.cIdx)
    dragState.value = { startY: t.clientY, pIdx: useIdx.pIdx, cIdx: useIdx.cIdx + 1 }
  }
}
function onChildTouchEnd() {
  dragState.value = { startY: 0, pIdx: -1, cIdx: -1 }
}

function deleteCategory(id, isParent) {
  uni.showModal({
    title: '确认删除',
    content: isParent ? '删除一级分类会同时删除其下所有二级分类，是否继续？' : '确定删除该二级分类？',
    success: async (res) => {
      if (!res.confirm) return
      if (loading.value) return
      loading.value = true
      try {
        await apiDeleteCategory(id)
        if (isParent) {
          const idsToRemove = [id, ...categories.value.filter(c => c.parentId === id).map(c => c.id)]
          categories.value = categories.value.filter(c => !idsToRemove.includes(c.id))
        } else {
          categories.value = categories.value.filter(c => c.id !== id)
        }
        uni.showToast({ title: '已删除' })
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.page { padding: 40rpx 32rpx; padding-bottom: 140rpx; }
.list {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.06);
}
.parent-item { border-bottom: 1rpx solid #f1f5f9; }
.parent-item:last-child { border-bottom: none; }
.parent-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 28rpx;
  background: #f8fafc;
}
.parent-name { font-size: 32rpx; font-weight: 600; color: #1e293b; }
.parent-actions { display: flex; gap: 28rpx; align-items: center; }
.btn-add { font-size: 26rpx; color: #3b82f6; font-weight: 500; }
.btn-del { font-size: 26rpx; color: #ef4444; }
.children-hint { padding: 14rpx 28rpx 0; font-size: 24rpx; color: #94a3b8; }
.children { padding: 0 28rpx; }
.child-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 26rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}
.child-item:last-child { border-bottom: none; }
.drag-handle { font-size: 28rpx; color: #94a3b8; margin-right: 20rpx; padding: 8rpx 0; }
.child-name { flex: 1; font-size: 28rpx; color: #475569; }
.actions { display: flex; gap: 28rpx; align-items: center; flex-wrap: wrap; }
.btn { font-size: 26rpx; color: #3b82f6; font-weight: 500; }
.child-item .btn-del { font-size: 26rpx; color: #ef4444; }
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 28rpx 32rpx;
  padding-bottom: calc(28rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -8rpx 32rpx rgba(59, 130, 246, 0.08);
}
.btn-add-parent {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.3);
}
.btn-add-parent:active { opacity: 0.92; }
.mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  z-index: 100;
}
.modal {
  position: fixed;
  left: 48rpx;
  right: 48rpx;
  top: 50%;
  transform: translateY(-50%);
  background: #fff;
  border-radius: 24rpx;
  padding: 44rpx 40rpx;
  z-index: 101;
  box-shadow: 0 24rpx 64rpx rgba(15, 23, 42, 0.2);
}
.modal-title { font-size: 34rpx; font-weight: 600; margin-bottom: 28rpx; color: #1e293b; }
.modal-input {
  border: 1rpx solid #e2e8f0;
  border-radius: 14rpx;
  padding: 26rpx 28rpx;
  font-size: 30rpx;
  margin-bottom: 36rpx;
  color: #1e293b;
}
.modal-btns { display: flex; gap: 24rpx; }
.modal-btn { flex: 1; height: 88rpx; line-height: 88rpx; border-radius: 14rpx; font-size: 30rpx; font-weight: 500; border: none; }
.cancel { background: #f1f5f9; color: #64748b; }
.ok { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: #fff; }
.ok:active { opacity: 0.9; }
</style>
