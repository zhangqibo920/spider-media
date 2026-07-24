import request from '@/utils/request'

export function getSystemConfigs(group: string) {
  return request.get('/admin/config', { params: { group } })
}

export function updateSystemConfig(key: string, value: string) {
  return request.put('/admin/config', { key, value })
}

export function getOperationLogs(page = 1, size = 20) {
  return request.get('/admin/logs', { params: { page, size } })
}
