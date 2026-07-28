import request from '@/utils/request'

export function getKeywords() {
  return request.get('/hotmonitor/keyword/list')
}

export function getKeyword(id: number) {
  return request.get(`/hotmonitor/keyword/${id}`)
}

export function createKeyword(data: any) {
  return request.post('/hotmonitor/keyword/create', data)
}

export function updateKeyword(data: any) {
  return request.put('/hotmonitor/keyword/update', data)
}

export function deleteKeyword(id: number) {
  return request.delete(`/hotmonitor/keyword/${id}`)
}

export function toggleKeywordStatus(id: number, status: string) {
  return request.put(`/hotmonitor/keyword/${id}/status`, null, { params: { status } })
}

export function getHotTopics(params?: any) {
  return request.get('/hotmonitor/topic/list', { params })
}

export function getTopicsByKeyword(keywordId: number) {
  return request.get(`/hotmonitor/topic/by-keyword/${keywordId}`)
}

export function getNotifications(isRead?: string) {
  return request.get('/hotmonitor/notification/list', { params: { isRead } })
}

export function getUnreadCount() {
  return request.get('/hotmonitor/notification/unread-count')
}

export function markNotificationRead(id: number) {
  return request.put(`/hotmonitor/notification/read/${id}`)
}

export function markAllNotificationsRead() {
  return request.put('/hotmonitor/notification/read-all')
}
