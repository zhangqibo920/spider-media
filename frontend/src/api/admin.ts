import request from '@/utils/request'

export function getSystemConfigs(group: string) {
  return request.get('/admin/config', { params: { group } })
}

export function addSystemConfig(data: any) {
  return request.post('/admin/config', data)
}

export function updateSystemConfig(config: any) {
  return request.put('/admin/config', config)
}

export function deleteSystemConfig(id: number) {
  return request.delete(`/admin/config/${id}`)
}

export function getUsers() {
  return request.get('/admin/users')
}

export function updateUser(user: any) {
  return request.put('/admin/users', user)
}

export function deleteUser(userId: number) {
  return request.delete(`/admin/users/${userId}`)
}

export function changePassword(userId: number, oldPassword: string, newPassword: string) {
  return request.put('/admin/users/password', { userId: String(userId), oldPassword, newPassword })
}

export function getOperationLogs(page = 1, size = 20) {
  return request.get('/admin/logs', { params: { page, size } })
}
