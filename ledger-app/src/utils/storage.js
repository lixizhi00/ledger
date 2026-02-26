const RECORDS_KEY = 'jizhang_records'
const CATEGORIES_KEY = 'jizhang_categories'
const BUDGET_MONTHLY_KEY = 'jizhang_budget_monthly'
const BUDGET_YEARLY_KEY = 'jizhang_budget_yearly'

export function getRecords() {
  try {
    const data = uni.getStorageSync(RECORDS_KEY)
    return data ? JSON.parse(data) : []
  } catch (e) {
    return []
  }
}

export function setRecords(list) {
  uni.setStorageSync(RECORDS_KEY, JSON.stringify(list))
}

export function addRecord(record) {
  const list = getRecords()
  const item = {
    id: Date.now() + '' + Math.random().toString(36).slice(2),
    ...record,
    createTime: Date.now()
  }
  list.push(item)
  setRecords(list)
  return item
}

export function getCategories() {
  try {
    const data = uni.getStorageSync(CATEGORIES_KEY)
    return data ? JSON.parse(data) : getDefaultCategories()
  } catch (e) {
    return getDefaultCategories()
  }
}

function getDefaultCategories() {
  const list = [
    { id: 'c1', name: '餐饮', parentId: null, sortOrder: 0 },
    { id: 'c2', name: '交通', parentId: null, sortOrder: 1 },
    { id: 'c3', name: '购物', parentId: null, sortOrder: 2 },
    { id: 'c4', name: '其他', parentId: null, sortOrder: 3 }
  ]
  setCategories(list)
  return list
}

export function setCategories(list) {
  uni.setStorageSync(CATEGORIES_KEY, JSON.stringify(list))
}

export function getBudgetMonthly() {
  try {
    const data = uni.getStorageSync(BUDGET_MONTHLY_KEY)
    return data ? JSON.parse(data) : {}
  } catch (e) {
    return {}
  }
}

export function getBudgetYearly() {
  try {
    const data = uni.getStorageSync(BUDGET_YEARLY_KEY)
    return data ? JSON.parse(data) : {}
  } catch (e) {
    return {}
  }
}

export function setBudgetMonthly(obj) {
  uni.setStorageSync(BUDGET_MONTHLY_KEY, JSON.stringify(obj))
}

export function setBudgetYearly(obj) {
  uni.setStorageSync(BUDGET_YEARLY_KEY, JSON.stringify(obj))
}
