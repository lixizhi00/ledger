import { getRecords } from './storage.js'
import { getTodayStr, getMonthStartStr, getYearStartStr, getMonthEndStr, getLastMonthStartStr, getLastMonthEndStr, parseDateStr } from './date.js'

export function getTotalByDateRange(startStr, endStr) {
  const records = getRecords()
  const start = parseDateStr(startStr).getTime()
  const end = parseDateStr(endStr).getTime() + 86400000
  return records
    .filter(r => {
      const t = parseDateStr(r.date).getTime()
      return t >= start && t < end
    })
    .reduce((sum, r) => sum + (Number(r.amount) || 0), 0)
}

export function getTodayTotal() {
  const today = getTodayStr()
  return getTotalByDateRange(today, today)
}

export function getMonthTotal() {
  const today = getTodayStr()
  const monthStart = getMonthStartStr()
  return getTotalByDateRange(monthStart, today)
}

export function getYearTotal() {
  const today = getTodayStr()
  const yearStart = getYearStartStr()
  return getTotalByDateRange(yearStart, today)
}

/** 按时间范围筛选记账列表（返回该时间范围内的记录，按日期倒序；可选 categoryId 按分类筛选） */
export function getRecordsByDateRange(startStr, endStr, categoryId) {
  const records = getRecords()
  const start = parseDateStr(startStr).getTime()
  const end = parseDateStr(endStr).getTime() + 86400000
  let list = records.filter(r => {
    const t = parseDateStr(r.date).getTime()
    return t >= start && t < end
  })
  if (categoryId) list = list.filter(r => r.categoryId === categoryId)
  return list.sort((a, b) => parseDateStr(b.date).getTime() - parseDateStr(a.date).getTime())
}

/** 某时间范围内某分类的总支出 */
export function getSpendingByCategoryInRange(startStr, endStr, categoryId) {
  const list = getRecordsByDateRange(startStr, endStr, categoryId)
  return list.reduce((sum, r) => sum + (Number(r.amount) || 0), 0)
}

/** 某月总支出，yearMonth 如 2025-01 */
export function getMonthSpending(yearMonth) {
  if (!yearMonth) return 0
  const [y, m] = yearMonth.split('-').map(Number)
  const start = `${y}-${String(m).padStart(2, '0')}-01`
  const end = getMonthEndStr(yearMonth)
  return getTotalByDateRange(start, end)
}

/** 上月结余（正为结余，负为超支），用于滚入本月 */
export function getLastMonthBalance(monthBudgetTotal) {
  const start = getLastMonthStartStr()
  const end = getLastMonthEndStr()
  const spent = getTotalByDateRange(start, end)
  return monthBudgetTotal - spent
}
