import request from '@/utils/request'
import { getDictDataByType, getDictTypeList } from '@/api/dict'

// ========== Dict Type ==========

export function addDictType(data: any) {
  return request.post('/dict/type', data)
}

export function updateDictType(data: any) {
  return request.put('/dict/type', data)
}

export function deleteDictType(id: number) {
  return request.delete(`/dict/type/${id}`)
}

// ========== Dict Data ==========

export function addDictData(data: any) {
  return request.post('/dict/data', data)
}

export function updateDictData(data: any) {
  return request.put('/dict/data', data)
}

export function deleteDictData(id: number) {
  return request.delete(`/dict/data/${id}`)
}
