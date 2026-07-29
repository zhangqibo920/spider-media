import { ref, watch } from 'vue'
import { getDictDataByType } from '@/api/dict'
import { DICT_FALLBACK } from '@/constants'

export interface DictData {
  id: number
  dictSort: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass: string
  listClass: string
  isDefault: string
  status: string
  remark: string
  createTime: string
}

const dictCache = new Map<string, DictData[]>()
const loadedSet = new Set<string>()
const versionMap = new Map<string, number>()

function loadDict(dictType: string): void {
  if (!dictCache.has(dictType)) {
    dictCache.set(dictType, DICT_FALLBACK[dictType] || [])
    versionMap.set(dictType, (versionMap.get(dictType) || 0) + 1)
  }

  if (loadedSet.has(dictType)) return

  loadedSet.add(dictType)

  getDictDataByType(dictType)
    .then(res => {
      const apiData = res.data || []
      if (apiData.length > 0) {
        dictCache.set(dictType, apiData)
      } else {
        console.warn(`Dict type "${dictType}" has no data in database, using fallback`)
      }
      versionMap.set(dictType, (versionMap.get(dictType) || 0) + 1)
    })
    .catch(err => {
      console.warn(`Dict API failed for "${dictType}", using fallback:`, err.message)
    })
}

export function reloadDict(dictType: string): void {
  loadedSet.delete(dictType)
  dictCache.delete(dictType)
  loadDict(dictType)
}

export function useDict(dictType: string) {
  const dict = ref<DictData[]>(dictCache.get(dictType) || DICT_FALLBACK[dictType] || [])

  loadDict(dictType)

  const stopWatch = watch(
    () => versionMap.get(dictType),
    () => {
      const newData = dictCache.get(dictType)
      if (newData) {
        dict.value = [...newData]
      }
    }
  )

  return { dict }
}

export function getDictLabel(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback?: string): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item ? item.dictLabel : (fallback ?? String(value))
}

export function getDictCssClass(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback = 'info'): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item?.cssClass || fallback
}
