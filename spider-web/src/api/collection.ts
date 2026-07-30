import request from '@/utils/request'

export function addTargetAccount(data: any) {
  return request.post('/collection/account', data)
}

export function updateTargetAccount(data: any) {
  return request.put('/collection/account', data)
}

export function getTargetAccounts() {
  return request.get('/collection/account/list')
}

export function deleteTargetAccount(id: number) {
  return request.delete(`/collection/account/${id}`)
}

export function triggerCollect(id: number) {
  return request.post(`/collection/account/${id}/collect`)
}

export function getCollectedArticles(id: number, page = 1, size = 20) {
  return request.get('/collection/article/page', { params: { targetAccountId: id, page, size } })
}

export function deleteCollectedArticle(id: number) {
  return request.delete(`/collection/article/${id}`)
}
