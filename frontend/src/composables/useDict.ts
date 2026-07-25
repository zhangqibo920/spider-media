import { ref, watch } from 'vue'
import { getDictDataByType } from '@/api/dict'
import { DICT_FALLBACK } from '@/constants'

/**
 * RuoYi-style dictionary composable
 *
 * Usage:
 *   const { dict } = useDict('sys_user_status')
 *   // dict.value = [{ dictLabel: '正常', dictValue: '0', cssClass: 'success', ... }]
 *
 *   const label = getDictLabel(dict, '0')  // => '正常'
 *   const type  = getDictType(dict, '0')   // => 'success'
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

/** Track which types are currently loading to avoid duplicate requests */
const loadingSet = new Set<string>()

/** All registered watchers (for cleanup) */
const watcherMap = new Map<string, ReturnType<typeof watch>>()

/**
 * Load dictionary data by type (with cache)
 */
function loadDict(dictType: string): void {
  if (dictCache.has(dictType) || loadingSet.has(dictType)) return
  loadingSet.add(dictType)

  getDictDataByType(dictType)
    .then(res => {
      dictCache.set(dictType, res.data || [])
    })
    .catch(() => {
      // Use fallback data when API is unavailable
      const fallback = DICT_FALLBACK[dictType]
      dictCache.set(dictType, fallback || [])
    })
    .finally(() => {
      loadingSet.delete(dictType)
    })
}

/**
 * useDict composable - provides reactive dictionary data for a given dictType
 *
 * @param dictType Dictionary type identifier (e.g. "sys_user_status")
 * @returns { dict: Ref<DictData[]> } reactive dictionary data array
 */
export function useDict(dictType: string) {
  const dict = ref<DictData[]>(dictCache.get(dictType) || [])

  // Load if not cached
  loadDict(dictType)

  // Watch cache updates
  const stopWatch = watch(
    () => dictCache.get(dictType),
    (newVal) => {
      dict.value = newVal || []
    },
    { deep: true }
  )

  watcherMap.set(dictType, stopWatch)

  return { dict }
}

/**
 * Get dictionary label by value from a loaded dict array
 *
 * @param dict Reactive dict data from useDict()
 * @param value Dictionary value to look up
 * @param fallback Fallback label if not found (defaults to original value)
 * @returns Display label
 */
export function getDictLabel(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback?: string): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item ? item.dictLabel : (fallback ?? String(value))
}

/**
 * Get dictionary CSS class (Element Plus Tag type) by value
 *
 * @param dict Reactive dict data from useDict()
 * @param value Dictionary value to look up
 * @param fallback Fallback class if not found (defaults to "info")
 * @returns Element Plus Tag type string
 */
export function getDictCssClass(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback = 'info'): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item?.cssClass || fallback
}
