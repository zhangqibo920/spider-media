import request from '@/utils/request'

export function addPlatformAccount(data: any) {
  return request.post('/publish/account', data)
}

export function updatePlatformAccount(data: any) {
  return request.put('/publish/account', data)
}

export function getPlatformAccounts() {
  return request.get('/publish/account/list')
}

export function deletePlatformAccount(id: number) {
  return request.delete(`/publish/account/${id}`)
}

export function createPublishTask(data: any) {
  return request.post('/publish/task', data)
}

export function updatePublishTask(data: any) {
  return request.put('/publish/task', data)
}

export function deletePublishTask(id: number) {
  return request.delete(`/publish/task/${id}`)
}

export function publishNow(id: number) {
  return request.post(`/publish/task/${id}/publish`)
}

export function schedulePublish(id: number, scheduledTime: string) {
  return request.post(`/publish/task/${id}/schedule`, { scheduledTime })
}

export function getPublishTasks(page = 1, size = 20) {
  return request.get('/publish/task/page', { params: { page, size } })
}
