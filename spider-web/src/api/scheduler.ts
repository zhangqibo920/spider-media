import request from '@/utils/request'

export function createScheduledTask(data: any) {
  return request.post('/scheduler/task', data)
}

export function updateScheduledTask(data: any) {
  return request.put('/scheduler/task', data)
}

export function deleteScheduledTask(id: number) {
  return request.delete(`/scheduler/task/${id}`)
}

export function enableTask(id: number) {
  return request.post(`/scheduler/task/${id}/enable`)
}

export function disableTask(id: number) {
  return request.post(`/scheduler/task/${id}/disable`)
}

export function getScheduledTasks(page = 1, size = 20) {
  return request.get('/scheduler/task/page', { params: { page, size } })
}
