import { get } from '@/utils/request.js'

/** 今日总支出 */
export function getTodayTotal() {
  return get('/api/stats/today')
}

/** 本月总支出 */
export function getMonthTotal() {
  return get('/api/stats/month')
}

/** 今年至今总支出 */
export function getYearTotal() {
  return get('/api/stats/year')
}

/** 汇总：today, month, year 及格式化 */
export function getStatsSummary() {
  return get('/api/stats/summary')
}
