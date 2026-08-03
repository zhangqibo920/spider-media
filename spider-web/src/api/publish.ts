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

export function testAccountConnection(id: number) {
  return request.post(`/publish/account/test/${id}`)
}

export function refreshAccountToken(id: number) {
  return request.post(`/publish/account/refresh/${id}`)
}

// 扫码登录相关接口
export function getToutiaoQrCode() {
  return request.get('/publish/qrcode/toutiao')
}

export function getBaijiahaoQrCode() {
  return request.get('/publish/qrcode/baijiahao')
}

export function pollToutiaoStatus(token: string) {
  return request.get(`/publish/qrcode/toutiao/poll/${token}`)
}

export function pollBaijiahaoStatus(token: string) {
  return request.get(`/publish/qrcode/baijiahao/poll/${token}`)
}

export function bindToutiaoCookie(data: { cookie: string; accountName?: string }) {
  return request.post('/publish/qrcode/toutiao/bind', data)
}

export function bindBaijiahaoCookie(data: { cookie: string; accountName?: string }) {
  return request.post('/publish/qrcode/baijiahao/bind', data)
}

// 发布任务相关接口
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
