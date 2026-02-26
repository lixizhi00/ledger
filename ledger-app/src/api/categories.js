import { get, post, put, del, patch } from '@/utils/request.js'

/** 获取分类列表 */
export function getCategories() {
  return get('/api/categories')
}

/** 新增或更新分类（无 id 为新增，有 id 为更新） */
export function saveCategory(data) {
  if (data.id) {
    return put(`/api/categories/${data.id}`, data)
  }
  return post('/api/categories', data)
}

/** 删除分类 */
export function deleteCategory(id) {
  return del(`/api/categories/${id}`)
}

/** 更新分类排序 */
export function updateCategorySort(id, sortOrder) {
  return patch(`/api/categories/${id}/sort`, { sortOrder })
}
