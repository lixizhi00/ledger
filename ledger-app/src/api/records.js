import { get, post, put, del } from '@/utils/request.js'

/** 新增支出记录 */
export function addRecord(data) {
  return post('/api/records', data)
}

/** 更新支出记录 */
export function updateRecord(id, data) {
  return put(`/api/records/${id}`, data)
}

/** 删除支出记录 */
export function deleteRecord(id) {
  return del(`/api/records/${id}`)
}

/** 按时间范围查询记录，categoryId 可选 */
export function getRecordsByDateRange(start, end, categoryId) {
  let url = `/api/records?start=${start}&end=${end}`
  if (categoryId) url += `&categoryId=${categoryId}`
  return get(url)
}
