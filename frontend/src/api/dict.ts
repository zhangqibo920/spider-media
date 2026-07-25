import request from '@/utils/request'

/**
 * RuoYi-style dictionary API
 */

/** Query dictionary data by dict type */
export function getDictDataByType(dictType: string) {
  return request.get(`/dict/data/type/${dictType}`)
}

/** Query all dictionary types */
export function getDictTypeList() {
  return request.get('/dict/type')
}
