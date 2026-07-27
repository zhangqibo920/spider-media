import request from '@/utils/request'

export function getRoles() {
  return request.get('/admin/role')
}

export function getRole(roleId: number) {
  return request.get(`/admin/role/${roleId}`)
}

export function addRole(data: any) {
  return request.post('/admin/role', data)
}

export function updateRole(data: any) {
  return request.put('/admin/role', data)
}

export function deleteRole(roleId: number) {
  return request.delete(`/admin/role/${roleId}`)
}

export function getRoleMenuIds(roleId: number) {
  return request.get(`/admin/role/${roleId}/menus`)
}

export function updateRoleMenus(roleId: number, menuIds: number[]) {
  return request.put(`/admin/role/${roleId}/menus`, { menuIds })
}
