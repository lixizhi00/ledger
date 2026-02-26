import { get, put } from '@/utils/request.js'

/** 获取预算（年 + 月） */
export function getBudget() {
  return get('/api/budget')
}

/** 更新年预算，传入 { categoryId: amount } */
export function updateYearlyBudget(yearly) {
  return put('/api/budget/yearly', yearly)
}
