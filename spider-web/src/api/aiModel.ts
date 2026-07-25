import request from '@/utils/request'

/** Get all AI models */
export function getAiModelList() {
  return request.get('/ai/model/list')
}

/** Get model by ID */
export function getAiModelById(id: number) {
  return request.get(`/ai/model/${id}`)
}

/** Add a new AI model */
export function addAiModel(data: any) {
  return request.post('/ai/model', data)
}

/** Update AI model */
export function updateAiModel(data: any) {
  return request.put('/ai/model', data)
}

/** Delete AI model */
export function deleteAiModel(id: number) {
  return request.delete(`/ai/model/${id}`)
}

/** Toggle model enabled/disabled */
export function toggleAiModel(id: number, enabled: string) {
  return request.put(`/ai/model/${id}/toggle`, { enabled })
}

/** Test model connectivity */
export function testAiModel(id: number) {
  return request.post(`/ai/model/${id}/test`)
}
