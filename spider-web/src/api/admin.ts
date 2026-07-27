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

export function resetPassword(userId: number, newPassword: string) {
  return request.put('/admin/users/password', { userId: String(userId), newPassword })
}

export function getOperationLogs(page = 1, size = 10) {
  return request.get('/admin/logs', { params: { page, size } })
}

export function getMenus() {
  return request.get('/admin/menus')
}

export function addMenu(data: any) {
  return request.post('/admin/menus', data)
}

export function updateMenu(data: any) {
  return request.put('/admin/menus', data)
}

export function deleteMenu(menuId: number) {
  return request.delete(`/admin/menus/${menuId}`)
}

export function getUserRoleIds(userId: number) {
  return request.get(`/admin/users/${userId}/roles`)
}

export function updateUserRoles(userId: number, roleIds: number[]) {
  return request.put(`/admin/users/${userId}/roles`, { roleIds })
}
