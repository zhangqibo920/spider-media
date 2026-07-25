import { ref, watch } from 'vue'
import { getDictDataByType } from '@/api/dict'
import { DICT_FALLBACK } from '@/constants'

/**
 * RuoYi-style dictionary composable
 *
 * Usage:
 *   const { dict } = useDict('sys_user_status')
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

/** Global cache: dictType -> DictData[] */
const dictCache = new Map<string, DictData[]>()
/** Track loaded types to avoid duplicate API calls */
const loadedSet = new Set<string>()
/** Version counter for reactivity trigger */
const versionMap = new Map<string, number>()

/**
 * Load dictionary data: fallback first (sync), then API update (async)
 */
function loadDict(dictType: string): void {
  // Always ensure fallback is available immediately
  if (!dictCache.has(dictType)) {
    dictCache.set(dictType, DICT_FALLBACK[dictType] || [])
    versionMap.set(dictType, (versionMap.get(dictType) || 0) + 1)
  }

  // Skip API call if already loaded successfully
  if (loadedSet.has(dictType)) return

  loadedSet.add(dictType)

  getDictDataByType(dictType)
    .then(res => {
      const apiData = res.data || []
      if (apiData.length > 0) {
        dictCache.set(dictType, apiData)
      } else {
        // API returned empty - mark as loaded but keep fallback
        console.warn(`Dict type "${dictType}" has no data in database, using fallback`)
      }
      versionMap.set(dictType, (versionMap.get(dictType) || 0) + 1)
    })
    .catch(err => {
      console.warn(`Dict API failed for "${dictType}", using fallback:`, err.message)
      // Fallback already in cache, no action needed
    })
}

/**
 * Force reload a dict type (e.g., after admin edits dict data)
 */
export function reloadDict(dictType: string): void {
  loadedSet.delete(dictType)
  dictCache.delete(dictType)
  loadDict(dictType)
}

/**
 * Reload all loaded dict types
 */
export function reloadAllDicts(): void {
  const types = Array.from(loadedSet)
  loadedSet.clear()
  dictCache.clear()
  types.forEach(t => loadDict(t))
}

/**
 * useDict composable
 */
export function useDict(dictType: string) {
  // Initialize with fallback data (synchronous, always available)
  const dict = ref<DictData[]>(dictCache.get(dictType) || DICT_FALLBACK[dictType] || [])

  // Trigger load (fallback is sync, API is async)
  loadDict(dictType)

  // Watch for data updates
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
 * Get dictionary CSS class by value
 */
export function getDictCssClass(dict: DictData[] | ReturnType<typeof ref<DictData[]>>, value: string, fallback = 'info'): string {
  const list = Array.isArray(dict) ? dict : dict.value
  const item = list.find(d => d.dictValue === String(value))
  return item?.cssClass || fallback
}
