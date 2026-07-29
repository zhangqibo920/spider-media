import request from '@/utils/request'

export function updateProfile(data: any) {
  return request.put('/user/profile', data)
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.put('/user/profile/password', { oldPassword, newPassword })
}
