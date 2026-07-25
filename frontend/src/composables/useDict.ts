import { ref, watch } from 'vue'
import { getDictDataByType } from '@/api/dict'
import { DICT_FALLBACK } from '@/constants'

/**
 * RuoYi-style dictionary composable
 *
 * Usage:
 *   const { dict } = useDict('sys_user_status')
 *   // dict.value = [{ dictLabel: '正常', dictValue: '0', cssClass: 'success', ... }]
 */

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

/** Global dictionary cache: dictType -> DictData[] */
const dictCache = new Map<string, DictData[]>()

/** Track which types are currently loading */
const loadingSet = new Set<string>()

/** Version counter for each dict type - increments on data change to trigger reactivity */
const versionMap = new Map<string, number>()

/**
 * Load dictionary data by type (with cache + fallback)
 *
 * Strategy: immediately populate cache with fallback data (synchronous),
 * then fetch from API and update cache when response arrives.
 * This ensures dict data is always available on first render.
 */
function loadDict(dictType: string): void {
  if (loadingSet.has(dictType)) return

  // Step 1: Immediately populate with fallback data if cache is empty
  if (!dictCache.has(dictType)) {
    const fallback = DICT_FALLBACK[dictType] || []
    dictCache.set(dictType, fallback)
    versionMap.set(dictType, (versionMap.get(dictType) || 0) + 1)
  }

  // Step 2: Fetch from API (skip if already loaded successfully)
  if (dictCache.has(dictType) && dictCache.get(dictType)!.length > 0 && dictCache.get(dictType) !== DICT_FALLBACK[dictType]) {
    return
  }

  loadingSet.add(dictType)

  getDictDataByType(dictType)
    .then(res => {
      const data = res.data || []
      if (data.length > 0) {
        dictCache.set(dictType, data)
      }
      // Increment version to trigger watchers
      versionMap.set(dictType, (versionMap.get(dictType) || 0) + 1)
    })
    .catch(() => {
      // Fallback already populated, no action needed
    })
    .finally(() => {
      loadingSet.delete(dictType)
    })
}

/**
 * useDict composable - provides reactive dictionary data
 */
export function useDict(dictType: string) {
  // Initialize with whatever is in cache (fallback or API data)
  const dict = ref<DictData[]>(dictCache.get(dictType) || DICT_FALLBACK[dictType] || [])

  // Load data (fallback is synchronous, API is async)
  loadDict(dictType)

  // Watch version counter changes to update dict reactively
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

/**
 * Get dictionary label by value
 */
export function getDictLabel(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback?: string): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item ? item.dictLabel : (fallback ?? String(value))
}

/**
 * Get dictionary CSS class (Element Plus Tag type) by value
 */
export function getDictCssClass(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback = 'info'): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item?.cssClass || fallback
}
