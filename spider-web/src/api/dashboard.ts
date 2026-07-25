import request from '@/utils/request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}
