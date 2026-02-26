export function getTodayStr() {
  const d = new Date()
  return formatDate(d)
}

export function getMonthStartStr() {
  const d = new Date()
  d.setDate(1)
  return formatDate(d)
}

export function getYearStartStr() {
  const d = new Date()
  d.setMonth(0)
  d.setDate(1)
  return formatDate(d)
}

export function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 中文日期，如 2026年2月25日 */
export function formatDateCN(dateStr) {
  if (!dateStr) return ''
  const [y, m, d] = dateStr.split('-').map(Number)
  return `${y}年${m}月${d}日`
}

/** 中文年月，如 2026年2月 */
export function formatYearMonthCN() {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
}

export function getYearMonth(dateStr) {
  return dateStr ? dateStr.slice(0, 7) : ''
}

export function getYear(dateStr) {
  return dateStr ? dateStr.slice(0, 4) : ''
}

export function parseDateStr(str) {
  if (!str) return new Date()
  const [y, m, d] = str.split('-').map(Number)
  return new Date(y, (m || 1) - 1, d || 1)
}

/** 某月最后一天 YYYY-MM-DD，传 yearMonth 如 2025-01 或空表当前月 */
export function getMonthEndStr(yearMonth) {
  let y, m
  if (yearMonth) {
    const [yy, mm] = yearMonth.split('-').map(Number)
    y = yy
    m = mm || 1
  } else {
    const d = new Date()
    y = d.getFullYear()
    m = d.getMonth() + 1
  }
  const last = new Date(y, m, 0)
  return formatDate(last)
}

/** 上月第一天 */
export function getLastMonthStartStr() {
  const d = new Date()
  d.setMonth(d.getMonth() - 1)
  d.setDate(1)
  return formatDate(d)
}

/** 上月最后一天 */
export function getLastMonthEndStr() {
  const d = new Date()
  d.setDate(0)
  return formatDate(d)
}
